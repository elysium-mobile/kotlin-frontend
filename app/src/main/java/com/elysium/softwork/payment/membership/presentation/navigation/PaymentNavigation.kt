package com.elysium.softwork.payment.membership.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elysium.softwork.payment.membership.presentation.views.methods.PaymentMethodsScreen
import com.elysium.softwork.payment.membership.presentation.views.newcard.NewCardScreen
import com.elysium.softwork.payment.membership.presentation.views.selection.MembershipSelectionScreen
import com.elysium.softwork.payment.membership.presentation.views.success.PaymentSuccessScreen

/**
 * Process-stable no-op callback shared by callers of [paymentGraph] that do not need a
 * post-activation side effect (the settings mount inside the authenticated shell).
 *
 * Declared once at class load so the reference is allocation-free across every
 * recomposition of the host that consumes it.
 */
val NoPaymentGraphExit: () -> Unit = {}

/**
 * Standalone payment NavHost mounted at the application root when an authenticated worker
 * lacks an active membership.
 *
 * Owns its own [NavHostController]; the navigation back stack therefore belongs
 * exclusively to the onboarding flow. The host occupies the full window via
 * [Modifier.fillMaxSize]; system-bar insets are consumed by individual screens at the
 * precise boundary where they matter (the status-bar gradient on success, the
 * navigation-bar inset on bottom-pinned CTAs).
 *
 * @param onExitToMainShell invoked from the success screen's primary action. The host
 *   above this composable typically gates app navigation on the membership store's
 *   reactive flag and swaps into the main shell automatically; the callback exists for
 *   any non-navigational side effect (analytics, logging) the caller wants to attach.
 */
@Composable
fun PaymentOnboardingHost(onExitToMainShell: () -> Unit) {
    val navController: NavHostController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PaymentRoutes.SELECTION,
        modifier = Modifier.fillMaxSize(),
    ) {
        paymentGraph(navController = navController, onExitToMainShell = onExitToMainShell)
    }
}

/**
 * Registers the payment and membership routes inside an existing [NavGraphBuilder].
 *
 * Invoked in two host contexts:
 *  - By [PaymentOnboardingHost] when the membership gate fires; start destination is
 *    [PaymentRoutes.SELECTION].
 *  - By the authenticated main shell so the profile "Payment methods" entry lands on the
 *    methods screen with `fromSettings = true` and the cancel action enabled. Callers in
 *    that context should pass [NoPaymentGraphExit] for [onExitToMainShell].
 *
 * Every per-route navigation lambda is built once via [remember] keyed on the stable
 * [navController] reference. The cached lambdas survive recompositions of the route
 * content, so per-frame allocations are zero.
 *
 * @param navController controller that backs every per-route navigation lambda.
 * @param onExitToMainShell invoked from the success screen's primary action. The
 *   onboarding host typically wires this to the gate observer; the settings mount passes
 *   [NoPaymentGraphExit].
 */
fun NavGraphBuilder.paymentGraph(
    navController: NavHostController,
    onExitToMainShell: () -> Unit,
) {
    composable(PaymentRoutes.SELECTION) {
        val onPlanSelected: (String) -> Unit = remember(navController) {
            { planKey ->
                navController.navigate(
                    PaymentRoutes.methods(planKey = planKey, fromSettings = false),
                )
            }
        }
        MembershipSelectionScreen(onPlanSelected = onPlanSelected)
    }

    composable(
        route = PaymentRoutes.METHODS,
        arguments = listOf(
            navArgument(PaymentRoutes.METHODS_ARG_PLAN_KEY) { type = NavType.StringType },
            navArgument(PaymentRoutes.METHODS_ARG_FROM_SETTINGS) { type = NavType.BoolType },
        ),
    ) { backStackEntry ->
        val args = backStackEntry.arguments
        val planKey: String = args?.getString(PaymentRoutes.METHODS_ARG_PLAN_KEY).orEmpty()
        val fromSettings: Boolean =
            args?.getBoolean(PaymentRoutes.METHODS_ARG_FROM_SETTINGS) == true

        val onBack: () -> Unit = remember(navController) { { navController.popBackStack() } }
        val onAddPaymentMethod: () -> Unit = remember(navController) {
            { navController.navigate(PaymentRoutes.NEW_CARD) }
        }
        val onPaymentSucceeded: (String) -> Unit = remember(navController) {
            { resolvedPlanKey -> navController.navigate(PaymentRoutes.success(resolvedPlanKey)) }
        }
        val onSubscriptionCancelled: () -> Unit = remember(navController) {
            { navController.popBackStack() }
        }

        PaymentMethodsScreen(
            planKey = planKey,
            fromSettings = fromSettings,
            onBack = onBack,
            onAddPaymentMethod = onAddPaymentMethod,
            onPaymentSucceeded = onPaymentSucceeded,
            onSubscriptionCancelled = onSubscriptionCancelled,
        )
    }

    composable(PaymentRoutes.NEW_CARD) {
        val onBack: () -> Unit = remember(navController) { { navController.popBackStack() } }
        val onCardAdded: () -> Unit = remember(navController) { { navController.popBackStack() } }
        NewCardScreen(onBack = onBack, onCardAdded = onCardAdded)
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
        PaymentSuccessScreen(planKey = planKey, onEnterMainShell = onExitToMainShell)
    }
}
