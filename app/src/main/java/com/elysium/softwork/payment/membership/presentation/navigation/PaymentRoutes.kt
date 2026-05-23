package com.elysium.softwork.payment.membership.presentation.navigation

/**
 * Route catalog for the payment & membership bounded context.
 *
 * The graph is mounted in two places:
 * - As a standalone onboarding host owned by `MainActivity` when an authenticated worker
 *   has no active membership (the gate). [SELECTION] is the start destination.
 * - Nested inside `MainNavHost` when the worker enters via Profile → "Payment methods". The
 *   entry route is then [methods] with [METHODS_ARG_FROM_SETTINGS] = `true`, which is what
 *   surfaces the "Cancel subscription" action.
 *
 * [METHODS_ARG_PLAN_KEY] = [CURRENT_PLAN_SENTINEL] tells the methods screen to resolve the
 * plan from `MembershipStore.currentPlanKey` instead of from the nav argument — used by the
 * settings entry where the worker already has an active plan.
 */
object PaymentRoutes {

    /** Plan selection screen. Start destination of the onboarding graph. */
    const val SELECTION: String = "payment/selection"

    // region Methods
    const val METHODS_ARG_PLAN_KEY: String = "planKey"
    const val METHODS_ARG_FROM_SETTINGS: String = "fromSettings"

    /**
     * Route template: `payment/methods/{planKey}/{fromSettings}`.
     *
     * `fromSettings` is a boolean string (`true` / `false`) — Compose Navigation reads it
     * back via `NavType.BoolType`.
     */
    const val METHODS: String =
        "payment/methods/{$METHODS_ARG_PLAN_KEY}/{$METHODS_ARG_FROM_SETTINGS}"

    /**
     * Sentinel passed as `planKey` when the methods screen should resolve the active plan
     * from `MembershipStore.currentPlanKey` (settings entry).
     */
    const val CURRENT_PLAN_SENTINEL: String = "current"

    /**
     * Builds a concrete methods route.
     *
     * @param planKey stable [com.elysium.softwork.payment.membership.domain.model.MembershipPlan.key]
     *   the worker just picked, or [CURRENT_PLAN_SENTINEL] when entering from settings.
     * @param fromSettings `true` when reached from Profile → "Payment methods"; controls
     *   the visibility of the "Cancel subscription" action.
     */
    fun methods(planKey: String, fromSettings: Boolean): String =
        "payment/methods/$planKey/$fromSettings"
    // endregion

    // region New card
    /** Add-card composer. The screen does not need to know about the entry context. */
    const val NEW_CARD: String = "payment/new-card"
    // endregion

    // region Success
    const val SUCCESS_ARG_PLAN_KEY: String = "planKey"

    /** Route template: `payment/success/{planKey}`. */
    const val SUCCESS: String = "payment/success/{$SUCCESS_ARG_PLAN_KEY}"

    /** Builds a concrete success route for [planKey]. */
    fun success(planKey: String): String = "payment/success/$planKey"
    // endregion
}
