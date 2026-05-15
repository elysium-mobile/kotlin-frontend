package com.elysium.softwork.worker.forum.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elysium.softwork.worker.forum.presentation.views.feed.ForumScreen
import com.elysium.softwork.worker.forum.presentation.views.newpost.NewPostScreen
import com.elysium.softwork.worker.forum.presentation.views.report.ForumReportScreen
import com.elysium.softwork.worker.forum.presentation.views.thread.ThreadScreen

/**
 * Route catalog for the Forum bounded context. The feed route is also the bottom-nav
 * destination, so [FEED] is referenced from
 * [com.elysium.softwork.shared.presentation.navigation.MainNavHost] when wiring the
 * "Forum" tab.
 */
object ForumRoutes {
    const val FEED: String = "forum/feed"
    const val NEW_POST: String = "forum/new-post"

    private const val THREAD_BASE: String = "forum/thread"
    const val THREAD_ARG_POST_ID: String = "postId"
    const val THREAD: String = "$THREAD_BASE/{$THREAD_ARG_POST_ID}"

    /** Builds a concrete thread route for the given [postId]. */
    fun thread(postId: String): String = "$THREAD_BASE/$postId"

    private const val REPORT_BASE: String = "forum/report"
    const val REPORT_ARG_POST_ID: String = "postId"
    const val REPORT: String = "$REPORT_BASE/{$REPORT_ARG_POST_ID}"

    /** Builds a concrete report route for the given [postId]. */
    fun report(postId: String): String = "$REPORT_BASE/$postId"
}

/**
 * Registers the forum routes inside an existing [NavGraphBuilder]. Called by `MainNavHost`
 * so the feed/new-post/thread destinations live on the same back stack as the rest of the
 * authenticated shell.
 *
 * @param navController controller used to build navigate / popBackStack lambdas.
 * @param userName threaded down to the new-post composer and the thread sticky input for
 *   the non-anonymous identity rendering. Lifted from `MainNavHost`.
 */
fun NavGraphBuilder.forumGraph(navController: NavHostController, userName: String) {
    composable(ForumRoutes.FEED) {
        ForumScreen(
            onNewPost = { navController.navigate(ForumRoutes.NEW_POST) },
            onOpenThread = { postId -> navController.navigate(ForumRoutes.thread(postId)) },
            onReportPost = { postId -> navController.navigate(ForumRoutes.report(postId)) },
        )
    }

    composable(ForumRoutes.NEW_POST) {
        NewPostScreen(
            userName = userName,
            onClose = { navController.popBackStack() },
            onPublished = { navController.popBackStack() },
        )
    }

    composable(
        route = ForumRoutes.THREAD,
        arguments = listOf(
            navArgument(ForumRoutes.THREAD_ARG_POST_ID) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val postId: String = backStackEntry.arguments
            ?.getString(ForumRoutes.THREAD_ARG_POST_ID)
            .orEmpty()
        ThreadScreen(
            postId = postId,
            userName = userName,
            onBack = { navController.popBackStack() },
            onReport = { id -> navController.navigate(ForumRoutes.report(id)) },
        )
    }

    composable(
        route = ForumRoutes.REPORT,
        arguments = listOf(
            navArgument(ForumRoutes.REPORT_ARG_POST_ID) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val postId: String = backStackEntry.arguments
            ?.getString(ForumRoutes.REPORT_ARG_POST_ID)
            .orEmpty()
        ForumReportScreen(
            postId = postId,
            onBack = { navController.popBackStack() },
        )
    }
}
