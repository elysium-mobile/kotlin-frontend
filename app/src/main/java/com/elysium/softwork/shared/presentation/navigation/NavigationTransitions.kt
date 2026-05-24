package com.elysium.softwork.shared.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * Transition profile catalogue shared by every SoftWork navigation graph.
 *
 * Two reusable patterns are exposed:
 *  - **Horizontal push** ([PushEnter], [PushExit], [PushPopEnter], [PushPopExit]) for
 *    hierarchical navigation (a screen that drills into a child screen). The incoming
 *    screen slides in from the leading edge while the outgoing screen recedes via a
 *    subtle scale-down — the parallax tug that a sibling slide would produce is
 *    expensive on weak GPUs because both screens must be drawn at full opacity during
 *    the transition. Scale-down keeps the outgoing screen visually present without
 *    spending the bandwidth of a full second slide.
 *  - **Tab cross-fade** ([TabEnter], [TabExit]) for top-level destinations reached from
 *    the bottom navigation bar. The incoming tab fades in while gently expanding from
 *    a 0.98 scale floor to 1.0 — the expansion masks the layout snap that would
 *    otherwise occur when sibling tabs have different intrinsic heights.
 *
 * **Why tween, not spring.** Physics-based specs continue to evaluate target coordinates
 * each frame until the velocity threshold is crossed; the resulting variable-duration
 * tail jitters on hardware that cannot guarantee 16 ms frame pacing. Time-based [tween]
 * specs always finish at the declared duration with a single deterministic evaluation
 * per frame, which keeps the animation budget predictable on the API 29 / 2-3 GB RAM
 * hardware floor this app targets.
 *
 * **Why Standard easing.** [FastOutSlowInEasing] (the official Material standard
 * easing) and [LinearOutSlowInEasing] (the decelerating reveal curve) are both cubic
 * Béziers. Their per-frame cost is constant, chipset-independent, and identical to the
 * curves used by the system back-stack animator — so app transitions feel consistent
 * with platform gestures.
 *
 * **Allocation discipline.** Every animation spec, slide direction and transition
 * factory is hoisted to a top-level `val`. Navigation builders therefore reference
 * cached singletons; no `tween(...)`, `fadeIn(...)` or `slideIntoContainer(...)` call
 * fires on the per-route registration path.
 */

/** Dominant duration for the horizontal push pattern. Tuned to feel responsive yet smooth. */
private const val PUSH_DURATION_MS: Int = 280

/** Dominant duration for the cross-fade pattern. Slightly shorter so tab swaps feel snappier. */
private const val TAB_DURATION_MS: Int = 250

/** Duration of the supporting fade-out that overlaps with the dominant slide/scale animation. */
private const val FADE_OUT_DURATION_MS: Int = 200

/** Scale floor used by the cross-fade pattern's gentle expansion. */
private const val TAB_INITIAL_SCALE: Float = 0.98f

/** Scale floor used by the push pattern's outgoing screen so it recedes rather than slides off. */
private const val PUSH_EXIT_SCALE: Float = 0.98f

/** Horizontal slide spec shared by every push transition. Symmetric standard easing. */
private val PushOffsetSpec = tween<IntOffset>(
    durationMillis = PUSH_DURATION_MS,
    easing = FastOutSlowInEasing,
)

/** Fade-in spec for the dominant incoming screen. Decelerating curve for a soft reveal. */
private val PushFadeInSpec = tween<Float>(
    durationMillis = PUSH_DURATION_MS,
    easing = LinearOutSlowInEasing,
)

/** Fade-out spec for the receding outgoing screen. Slightly shorter to clear the frame fast. */
private val PushFadeOutSpec = tween<Float>(
    durationMillis = FADE_OUT_DURATION_MS,
    easing = FastOutSlowInEasing,
)

/** Scale spec for the push pattern's outgoing screen. */
private val PushScaleSpec = tween<Float>(
    durationMillis = PUSH_DURATION_MS,
    easing = FastOutSlowInEasing,
)

/** Fade-in spec for the cross-fade pattern's incoming tab. */
private val TabFadeInSpec = tween<Float>(
    durationMillis = TAB_DURATION_MS,
    easing = LinearOutSlowInEasing,
)

/** Fade-out spec for the cross-fade pattern's outgoing tab. */
private val TabFadeOutSpec = tween<Float>(
    durationMillis = FADE_OUT_DURATION_MS,
    easing = FastOutSlowInEasing,
)

/** Scale spec for the cross-fade pattern's incoming tab expansion. */
private val TabScaleSpec = tween<Float>(
    durationMillis = TAB_DURATION_MS,
    easing = LinearOutSlowInEasing,
)

/**
 * Enter transition for the horizontal push pattern.
 *
 * The incoming screen slides in from the trailing (right) edge while fading in. Fires
 * when the worker navigates forward through a hierarchical flow (e.g. Feed → Thread,
 * Selection → Methods, Profile → Protected identity).
 *
 * Using the standalone [slideInHorizontally] rather than `AnimatedContentTransitionScope.slideIntoContainer`
 * avoids reading the container width on each evaluation; the offset producer is a
 * single multiplication on the measured width and returns `IntOffset.Zero` at the end.
 */
val PushEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = PushOffsetSpec, initialOffsetX = { width -> width }) +
        fadeIn(animationSpec = PushFadeInSpec)
}

/**
 * Exit transition for the horizontal push pattern.
 *
 * The outgoing screen recedes via a subtle scale-down to [PUSH_EXIT_SCALE] and fades
 * out. The scale-only treatment — rather than a second sibling slide — halves the
 * pixels rewritten during the transition, which is the dominant cost on devices with
 * a software-rasterized compositor.
 */
val PushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    scaleOut(targetScale = PUSH_EXIT_SCALE, animationSpec = PushScaleSpec) +
        fadeOut(animationSpec = PushFadeOutSpec)
}

/**
 * Pop-enter transition for the horizontal push pattern.
 *
 * The destination slides back in from the leading (left) edge while fading in. Fires
 * when the back stack pops a child screen and the parent re-enters view.
 */
val PushPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(animationSpec = PushOffsetSpec, initialOffsetX = { width -> -width }) +
        fadeIn(animationSpec = PushFadeInSpec)
}

/**
 * Pop-exit transition for the horizontal push pattern.
 *
 * The popped screen slides out to the trailing (right) edge while fading out. Mirrors
 * [PushEnter] and fires when the worker navigates back through the hierarchy.
 */
val PushPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(animationSpec = PushOffsetSpec, targetOffsetX = { width -> width }) +
        fadeOut(animationSpec = PushFadeOutSpec)
}

/**
 * Enter transition for top-level tab destinations.
 *
 * The incoming tab fades in while expanding from [TAB_INITIAL_SCALE] to 1.0. The
 * subtle scale-up masks the layout snap that would otherwise occur when sibling tabs
 * have different intrinsic content heights (e.g. an empty notifications list vs. a
 * populated forum feed).
 *
 * Pop-enter falls back to this same transition because tab destinations are never
 * "popped back into" via the system back gesture — the bottom bar uses
 * `launchSingleTop = true` and `restoreState = true`, so re-selecting a tab triggers
 * the normal enter path.
 */
val TabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(animationSpec = TabFadeInSpec) +
        scaleIn(initialScale = TAB_INITIAL_SCALE, animationSpec = TabScaleSpec)
}

/**
 * Exit transition for top-level tab destinations.
 *
 * A plain fade-out — the outgoing tab stays in place to avoid any visual conflict
 * with the incoming tab's scale expansion, which keeps the cross-fade visually clean
 * even when the two tabs render content of different heights.
 */
val TabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = TabFadeOutSpec)
}
