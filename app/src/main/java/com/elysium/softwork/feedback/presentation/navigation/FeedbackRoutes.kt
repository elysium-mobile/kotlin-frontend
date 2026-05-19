package com.elysium.softwork.feedback.presentation.navigation

/**
 * Route catalog for the Feedback bounded context. Kept in its own file so other navigation
 * hosts can import the constants without pulling in the `NavGraphBuilder` extension defined
 * in `FeedbackNavigation.kt`.
 */
object FeedbackRoutes {
    const val PENDING_SURVEYS: String = "feedback/pending_surveys"
}
