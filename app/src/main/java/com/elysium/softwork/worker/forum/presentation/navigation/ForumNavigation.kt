package com.elysium.softwork.worker.forum.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elysium.softwork.shared.presentation.navigation.PushEnter
import com.elysium.softwork.shared.presentation.navigation.PushExit
import com.elysium.softwork.shared.presentation.navigation.PushPopEnter
import com.elysium.softwork.shared.presentation.navigation.PushPopExit
import com.elysium.softwork.shared.presentation.navigation.TabEnter
import com.elysium.softwork.shared.presentation.navigation.TabExit
import com.elysium.softwork.worker.forum.presentation.views.feed.ForumScreen
import com.elysium.softwork.worker.forum.presentation.views.newpost.NewPostScreen
import com.elysium.softwork.worker.forum.presentation.views.report.ForumReportScreen
import com.elysium.softwork.worker.forum.presentation.views.reports.ReportsStatusScreen
import com.elysium.softwork.worker.forum.presentation.views.thread.ThreadScreen

/**
 * Registers the forum routes inside an existing [NavGraphBuilder]. Called by `MainNavHost`
 * so the feed/new-post/thread destinations live on the same back stack as the rest of the
 * authenticated shell.
 *
 * Route catalog lives in [ForumRoutes].
 *
 * @param navController controller used to build navigate / popBackStack lambdas.
 * @param userName threaded down to the new-post composer and the thread sticky input for
 *   the non-anonymous identity rendering. Lifted from `MainNavHost`.
 */
fun NavGraphBuilder.forumGraph(navController: NavHostController, userName: String) {
    composable(
        route = ForumRoutes.FEED,
        enterTransition = TabEnter,
        exitTransition = TabExit,
        popEnterTransition = TabEnter,
        popExitTransition = TabExit,
    ) {
        ForumScreen(
            onNewPost = { navController.navigate(ForumRoutes.NEW_POST) },
            onOpenThread = { threadId -> navController.navigate(ForumRoutes.thread(threadId)) },
            onReportPost = { threadId -> navController.navigate(ForumRoutes.report(threadId)) },
        )
    }

    composable(
        route = ForumRoutes.NEW_POST,
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) {
        NewPostScreen(
            userName = userName,
            onClose = { navController.popBackStack() },
            onPublished = { navController.popBackStack() },
        )
    }

    composable(
        route = ForumRoutes.THREAD,
        arguments = listOf(
            navArgument(ForumRoutes.THREAD_ARG_THREAD_ID) { type = NavType.LongType },
        ),
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) { backStackEntry ->
        val threadId: Long = backStackEntry.arguments
            ?.getLong(ForumRoutes.THREAD_ARG_THREAD_ID)
            ?: 0L
        ThreadScreen(
            threadId = threadId,
            userName = userName,
            onBack = { navController.popBackStack() },
            onReport = { id -> navController.navigate(ForumRoutes.report(id)) },
        )
    }

    composable(
        route = ForumRoutes.REPORT,
        arguments = listOf(
            navArgument(ForumRoutes.REPORT_ARG_THREAD_ID) { type = NavType.LongType },
        ),
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) { backStackEntry ->
        val threadId: Long = backStackEntry.arguments
            ?.getLong(ForumRoutes.REPORT_ARG_THREAD_ID)
            ?: 0L
        ForumReportScreen(
            postId = threadId.toString(),
            onBack = { navController.popBackStack() },
        )
    }

    composable(
        route = ForumRoutes.REPORTS_STATUS,
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) {
        ReportsStatusScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
