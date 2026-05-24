package com.elysium.softwork.shared.presentation.navigation

import com.elysium.softwork.worker.forum.presentation.navigation.ForumRoutes

/**
 * Route catalog for the authenticated shell.
 *
 * Extracted from the navigation host file so feature contexts can import the catalog
 * without pulling in the host composable. The forum bottom tab points at
 * [ForumRoutes.FEED] directly, so this catalog does not duplicate that route — it lives
 * in the forum context's own catalog as the single source of truth.
 */
object MainRoutes {

    /** Home tab — first destination after authentication and an active membership. */
    const val HOME: String = "main/home"

    /** Profile tab — settings, identity protection entry, and the sign-out affordance. */
    const val PROFILE: String = "main/profile"

    /** Notifications tab — feed of inbox-style alerts. */
    const val NOTIFICATIONS: String = "main/notifications"

    /** Auxiliary destination reached from Profile to manage cross-context anonymity. */
    const val PROTECTED_IDENTITY: String = "main/protected-identity"

    /** Auxiliary destination reached from Home to start a pending survey. */
    const val SURVEYS: String = "main/surveys"
}
