package com.elysium.softwork.payment.membership.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elysium.softwork.payment.membership.presentation.views.methods.PaymentMethodsScreen
import com.elysium.softwork.payment.membership.presentation.views.newcard.NewCardScreen
import com.elysium.softwork.payment.membership.presentation.views.selection.MembershipSelectionScreen
import com.elysium.softwork.payment.membership.presentation.views.success.PaymentSuccessScreen

/**
 * Registers the payment & membership routes inside an existing [NavGraphBuilder].
 *
 * Invoked twice — once by the standalone onboarding host that `MainActivity` mounts when an
 * authenticated worker lacks a membership, and once by `MainNavHost` so the settings entry
 * point from Profile lands on the methods screen with the cancel action enabled.
 *
 * Route catalog lives in [PaymentRoutes].
 *
 * @param navController controller used to build navigate / popBackStack lambdas.
 * @param onExitToMainShell invoked from `PaymentSuccessScreen` once the worker taps
 *   "Main menu". The host is expected to flip the membership gate and let the Activity-level
 *   state collector swap the user into the main app shell. Wired only on the onboarding
 *   mount — the settings mount passes a no-op since the worker is already in the shell.
 */
fun NavGraphBuilder.paymentGraph(
    navController: NavHostController,
    onExitToMainShell: () -> Unit,
) {
    composable(PaymentRoutes.SELECTION) {
        MembershipSelectionScreen(
            onPlanSelected = { planKey ->
                navController.navigate(
                    PaymentRoutes.methods(planKey = planKey, fromSettings = false),
                )
            },
        )
    }

    composable(
        route = PaymentRoutes.METHODS,
        arguments = listOf(
            navArgument(PaymentRoutes.METHODS_ARG_PLAN_KEY) { type = NavType.StringType },
            navArgument(PaymentRoutes.METHODS_ARG_FROM_SETTINGS) { type = NavType.BoolType },
        ),
    ) { backStackEntry ->
        val planKey: String = backStackEntry.arguments
            ?.getString(PaymentRoutes.METHODS_ARG_PLAN_KEY)
            .orEmpty()
        val fromSettings: Boolean = backStackEntry.arguments
            ?.getBoolean(PaymentRoutes.METHODS_ARG_FROM_SETTINGS) == true

        PaymentMethodsScreen(
            planKey = planKey,
            fromSettings = fromSettings,
            onBack = { navController.popBackStack() },
            onAddPaymentMethod = { navController.navigate(PaymentRoutes.NEW_CARD) },
            onPaymentSucceeded = { resolvedKey ->
                navController.navigate(PaymentRoutes.success(resolvedKey))
            },
        )
    }

    composable(PaymentRoutes.NEW_CARD) {
        NewCardScreen(
            onBack = { navController.popBackStack() },
            onCardAdded = { navController.popBackStack() },
        )
    }

    composable(
        route = PaymentRoutes.SUCCESS,
        arguments = listOf(
            navArgument(PaymentRoutes.SUCCESS_ARG_PLAN_KEY) { type = NavType.StringType },
        ),
    ) { backStackEntry ->
        val planKey: String = backStackEntry.arguments
            ?.getString(PaymentRoutes.SUCCESS_ARG_PLAN_KEY)
            .orEmpty()
        PaymentSuccessScreen(
            planKey = planKey,
            onEnterMainShell = onExitToMainShell,
        )
    }
}
