package com.elysium.softwork.iam.presentation.navigation

import com.elysium.softwork.shared.utils.discriminators.SuccessKind

/**
 * Route definitions for the IAM bounded context. Centralized as constants so navigation
 * call sites are typo-safe and refactor-friendly.
 *
 * Extracted from `AuthNavigation.kt` so the routing catalog and the `NavGraphBuilder` wiring
 * can evolve independently. Composables and other navigation hosts should import this object
 * only — they never depend on the `AuthNavHost` composable.
 */
object AuthRoutes {
    const val LOGIN: String = "auth/login"
    const val REGISTER: String = "auth/register"
    const val REGISTER_GOOGLE: String = "auth/register-google"

    /** Success route accepts a kind discriminator: [SuccessKind.LOGIN] or [SuccessKind.REGISTER]. */
    private const val SUCCESS_BASE: String = "auth/success"
    const val SUCCESS_ARG_KIND: String = "kind"
    const val SUCCESS: String = "$SUCCESS_BASE/{$SUCCESS_ARG_KIND}"

    /** Builds a concrete route for the success screen. */
    fun success(kind: SuccessKind): String = "$SUCCESS_BASE/${kind.name}"
}
