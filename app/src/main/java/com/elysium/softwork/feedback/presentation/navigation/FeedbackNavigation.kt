package com.elysium.softwork.feedback.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.elysium.softwork.feedback.presentation.views.chat.AiChatScreen
import com.elysium.softwork.feedback.presentation.views.surveys.PendingSurveysScreen
import com.elysium.softwork.shared.presentation.navigation.PushEnter
import com.elysium.softwork.shared.presentation.navigation.PushExit
import com.elysium.softwork.shared.presentation.navigation.PushPopEnter
import com.elysium.softwork.shared.presentation.navigation.PushPopExit

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
    composable(
        route = FeedbackRoutes.PENDING_SURVEYS,
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) {
        PendingSurveysScreen(
            onBack = { navController.popBackStack() },
            onStartSurvey = { /* The answer flow is not yet wired. */ },
        )
    }

    composable(
        route = FeedbackRoutes.AI_CHAT,
        enterTransition = PushEnter,
        exitTransition = PushExit,
        popEnterTransition = PushPopEnter,
        popExitTransition = PushPopExit,
    ) {
        AiChatScreen(onBack = { navController.popBackStack() })
    }
}
