package com.elysium.softwork.worker.forum.presentation.navigation

/**
 * Route catalog for the Forum bounded context. The feed route is also the bottom-nav
 * destination, so [FEED] is referenced from
 * `com.elysium.softwork.shared.presentation.navigation.MainNavHost` when wiring the "Forum"
 * tab.
 *
 * Extracted from `ForumNavigation.kt` so other navigation hosts can import the constants
 * without pulling in the `NavGraphBuilder` extension.
 */
object ForumRoutes {
    const val FEED: String = "forum/feed"
    const val NEW_POST: String = "forum/new-post"

    private const val THREAD_BASE: String = "forum/thread"
    const val THREAD_ARG_THREAD_ID: String = "threadId"
    const val THREAD: String = "$THREAD_BASE/{$THREAD_ARG_THREAD_ID}"

    /** Builds a concrete thread route for the given [threadId]. */
    fun thread(threadId: Long): String = "$THREAD_BASE/$threadId"

    private const val REPORT_BASE: String = "forum/report"
    const val REPORT_ARG_THREAD_ID: String = "threadId"
    const val REPORT: String = "$REPORT_BASE/{$REPORT_ARG_THREAD_ID}"

    /** Builds a concrete report route for the given [threadId]. */
    fun report(threadId: Long): String = "$REPORT_BASE/$threadId"

    /** Read-only list of the user's submitted reports + their current status. */
    const val REPORTS_STATUS: String = "forum/reports-status"
}
