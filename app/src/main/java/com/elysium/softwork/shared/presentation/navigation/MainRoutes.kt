package com.elysium.softwork.shared.presentation.navigation

import com.elysium.softwork.worker.forum.presentation.navigation.ForumRoutes

/**
 * Routes for the authenticated shell.
 *
 * Extracted from `MainNavigation.kt` so feature contexts can import the catalog without
 * pulling in the `MainNavHost` composable. The Foro bottom tab points at [ForumRoutes.FEED]
 * directly, so this catalog does not duplicate that route — it's documented here as a
 * reference instead.
 */
object MainRoutes {
    const val HOME: String = "main/home"
    const val PROFILE: String = "main/profile"
    const val NOTIFICATIONS: String = "main/notifications"
    const val PROTECTED_IDENTITY: String = "main/protected-identity"
    const val SURVEYS: String = "main/surveys"
    const val MEMBERSHIP: String = "main/membership"
}
