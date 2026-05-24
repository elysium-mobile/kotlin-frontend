package com.elysium.softwork.notifications.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.elysium.softwork.notifications.presentation.views.feed.NotificationsScreen
import com.elysium.softwork.shared.presentation.navigation.TabEnter
import com.elysium.softwork.shared.presentation.navigation.TabExit

/**
 * Registers the Notifications routes inside an existing [NavGraphBuilder]. Invoked from
 * `MainNavHost` so the feed destination lives on the same back stack as the rest of the
 * authenticated shell.
 *
 * Route catalog lives in [NotificationRoutes].
 *
 * @param navController controller used to build navigate / popBackStack lambdas. Reserved
 *   for future destinations (e.g. tapping a notification → context-specific deep-link).
 */
fun NavGraphBuilder.notificationGraph(navController: NavHostController) {
    composable(
        route = NotificationRoutes.NOTIFICATIONS_FEED,
        enterTransition = TabEnter,
        exitTransition = TabExit,
        popEnterTransition = TabEnter,
        popExitTransition = TabExit,
    ) {
        NotificationsScreen(
            onNotificationClick = { /* Per-type deep-link routing is not yet wired. */ },
        )
    }
}
