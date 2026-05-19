package com.elysium.softwork.feedback.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.elysium.softwork.feedback.presentation.views.surveys.PendingSurveysScreen

/**
 * Registers the Feedback routes inside an existing [NavGraphBuilder]. Invoked from the host
 * `NavHost` (typically `MainNavHost`) so the surveys destination lives on the same back stack
 * as the rest of the authenticated shell.
 *
 * Route catalog lives in [FeedbackRoutes].
 *
 * @param navController controller used to build navigate / popBackStack lambdas.
 */
fun NavGraphBuilder.feedbackGraph(navController: NavHostController) {
    composable(FeedbackRoutes.PENDING_SURVEYS) {
        PendingSurveysScreen(
            onBack = { navController.popBackStack() },
            onStartSurvey = { /* Phase 5 — wires to the answer flow. */ },
        )
    }
}
