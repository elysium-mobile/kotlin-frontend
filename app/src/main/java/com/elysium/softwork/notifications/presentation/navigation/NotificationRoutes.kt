package com.elysium.softwork.notifications.presentation.navigation

/**
 * Route catalog for the Notifications bounded context. Kept in its own file so other
 * navigation hosts can import the constants without pulling in the `NavGraphBuilder`
 * extension defined in `NotificationNavigation.kt`.
 *
 * [NOTIFICATIONS_FEED] is the bottom-nav destination — referenced from
 * `MainNavHost`'s `BottomDestinations`.
 */
object NotificationRoutes {
    const val NOTIFICATIONS_FEED: String = "notifications/feed"
}
