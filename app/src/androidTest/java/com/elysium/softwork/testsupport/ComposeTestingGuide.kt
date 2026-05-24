package com.elysium.softwork.testsupport

/**
 * Standalone reference for Compose UI tests in this codebase.
 *
 * ## Why instrumentation, not Robolectric
 *
 * Compose UI tests need the real Android Choreographer, the real Skia renderer, and a
 * real `Activity` to host their composition. Robolectric simulates only a thin slice of
 * each, so its Compose support is consistently behind the stable Compose release and
 * tends to disagree with the device on layout pass ordering. This codebase therefore
 * runs Compose UI tests under [androidx.compose.ui.test.junit4.AndroidComposeTestRule]
 * on a device or emulator only — placed under `src/androidTest/`.
 *
 * The split is enforced structurally: anything that constructs a `ComposeContentTestRule`
 * lives under `src/androidTest/`, anything that constructs a `MainDispatcherRule` for a
 * pure ViewModel/coroutine test lives under `src/test/`. No file mixes the two test
 * source sets.
 *
 * ## Boilerplate
 *
 * Every Compose UI test method follows the same template, built around the v2
 * [androidx.compose.ui.test.v2.runComposeUiTest] builder. Both the legacy
 * `createComposeRule()` JUnit4 rule and the first-generation `runComposeUiTest`
 * builder have been deprecated; the v2 builder replaces them with a method-scoped
 * `ComposeUiTest` receiver whose API surface (`setContent`, `onNodeWithText`,
 * `waitForIdle`, `runOnUiThread`, `mainClock`, …) is identical, but its dispatcher
 * aligns with standard coroutine behavior by queuing tasks instead of executing
 * them eagerly. Pair every state mutation with the implicit synchronization the
 * matchers provide, or call `waitForIdle()` explicitly when asserting in between.
 *
 * ```
 * class MyScreenTest {
 *     @Test fun primary_action_emits_click() = runComposeUiTest {
 *         var clicked = false
 *         setContent {
 *             SoftWorkTheme {
 *                 SoftWorkButton(text = "Pay", onClick = { clicked = true })
 *             }
 *         }
 *         onNodeWithText("Pay").performClick()
 *         assertTrue(clicked)
 *     }
 * }
 * ```
 *
 * Notes:
 *  - The builder provisions an empty host Activity. When a screen requires
 *    Activity-level APIs (`onBackPressed`, IME insets that depend on the window), use
 *    `runAndroidComposeUiTest<ComponentActivity> { ... }` instead.
 *  - Always wrap the content under `SoftWorkTheme` so the assertions exercise the same
 *    palette and typography the production app renders.
 *  - Localized strings are looked up through the host Activity's `getString(R.string.x)`
 *    to keep assertions independent of the test locale. Inside the builder, the
 *    Activity is reachable via the implicit `activity` property on the
 *    `AndroidComposeUiTest` flavour.
 *
 * ## Querying the semantics tree
 *
 * The Compose test rule exposes the semantics tree — a stable abstraction layered above
 * the layout tree, indexed by accessibility properties. Preferred finders in order:
 *
 *  1. `onNodeWithText("Pay membership")` — best for buttons and headers.
 *  2. `onNodeWithContentDescription(R.string.cd_back)` — best for icon-only affordances
 *     such as the back arrows in every screen header.
 *  3. `onNodeWithTag("plan_card_pro")` — last resort. Tags must be added via
 *     `Modifier.testTag(...)` directly on the production composable and require a
 *     dedicated review because they leak test-only knowledge into the production source.
 *
 * Use `printToLog("MyScreenTest")` when an assertion fails — it dumps the entire
 * semantics tree with node hierarchy, helping localize the missing affordance.
 *
 * ## Asserting state-driven UI
 *
 * Compose recomposes asynchronously. The test rule provides two guarantees:
 *  - `composeRule.waitForIdle()` blocks until the composition has caught up with all
 *    pending state writes; called automatically before every assertion.
 *  - `composeRule.mainClock.advanceTimeBy(durationMs)` advances the animation clock by
 *    a deterministic step — required when asserting state mid-animation.
 *
 * ## Verifying lifecycle-aware collection
 *
 * `collectAsStateWithLifecycle()` is the project's lifecycle-safe replacement for
 * `collectAsState()`. The instrumentation suite verifies its semantics by driving a
 * [androidx.lifecycle.testing.TestLifecycleOwner] across the `STARTED` ↔ `CREATED`
 * boundary while emitting values into a `MutableStateFlow`, then asserting that the
 * composition only observes the emissions that happened while the lifecycle was at
 * least `STARTED`. See `LifecycleAwareCollectionTest` for the canonical implementation.
 *
 * Why this matters on the 2-3 GB RAM hardware floor: when the host Activity moves to
 * `STOPPED` (worker backgrounds the app), every active `StateFlow` collector must
 * pause. Otherwise, a busy producer in the data layer keeps draining the dispatcher,
 * burns CPU, and prevents the OS from suspending the process — directly impacting
 * battery and the system's ability to evict the app from memory cleanly. The
 * instrumentation test is the only reliable way to prove the contract because it
 * requires a real `LifecycleRegistry` evaluating its observers.
 *
 * ## Performance budget for the test suite
 *
 * Each test should hold under a 500 ms wall-clock budget on the slowest CI emulator
 * (API 29, 2 GB RAM). Two practices help:
 *  - Use `mainClock.autoAdvance = false` only when explicitly asserting on animation
 *    frames. The default (`true`) lets the rule fast-forward through layout passes.
 *  - Avoid `Thread.sleep` in any form. `waitUntil(timeoutMillis) { predicate }` and
 *    `mainClock.advanceTimeBy(...)` are deterministic substitutes.
 *
 * This guide is intentionally plain Kotlin (no `@Test` methods) so it compiles into the
 * androidTest classpath and stays discoverable by IDE navigation alongside the test
 * code it documents.
 */
object ComposeTestingGuide
