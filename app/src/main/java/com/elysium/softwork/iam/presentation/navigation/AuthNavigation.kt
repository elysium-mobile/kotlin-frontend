package com.elysium.softwork.iam.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elysium.softwork.R
import com.elysium.softwork.iam.presentation.views.login.LoginScreen
import com.elysium.softwork.iam.presentation.views.register.RegisterGoogleScreen
import com.elysium.softwork.iam.presentation.views.register.RegisterScreen
import com.elysium.softwork.iam.presentation.views.success.AuthSuccessScreen
import com.elysium.softwork.shared.utils.discriminators.SuccessKind

/**
 * IAM nav host. Designed to be embedded inside a future root nav graph but works stand-alone
 * for Phase 2 — `MainActivity` calls [AuthNavHost] directly until additional contexts (forum,
 * feedback) come online.
 *
 * Route catalog lives in [AuthRoutes]; the [SuccessKind] discriminator that drives the
 * `auth/success/{kind}` nav-arg lives in `shared/utils/discriminators/`.
 *
 * @param onAuthComplete invoked when the user reaches the success screen and taps the
 *   primary button. The hosting graph routes to the main app shell from here.
 */
@Composable
fun AuthNavHost(
    onAuthComplete: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AuthRoutes.LOGIN) {

        composable(AuthRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AuthRoutes.success(SuccessKind.LOGIN)) {
                        popUpTo(AuthRoutes.LOGIN) { inclusive = false }
                    }
                },
                onNavigateToRegister = { navController.navigate(AuthRoutes.REGISTER) },
                onNavigateToRegisterWithGoogle = { navController.navigate(AuthRoutes.REGISTER_GOOGLE) },
                onForgotPassword = { /* Phase 2: forgot-password flow not yet implemented. */ },
            )
        }

        composable(AuthRoutes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(AuthRoutes.success(SuccessKind.REGISTER)) {
                        popUpTo(AuthRoutes.LOGIN)
                    }
                },
            )
        }

        composable(AuthRoutes.REGISTER_GOOGLE) {
            RegisterGoogleScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(AuthRoutes.success(SuccessKind.REGISTER)) {
                        popUpTo(AuthRoutes.LOGIN)
                    }
                },
            )
        }

        composable(
            route = AuthRoutes.SUCCESS,
            arguments = listOf(navArgument(AuthRoutes.SUCCESS_ARG_KIND) { type = NavType.StringType }),
        ) { backStackEntry ->
            val kind: SuccessKind = backStackEntry.arguments
                ?.getString(AuthRoutes.SUCCESS_ARG_KIND)
                ?.let { runCatching { SuccessKind.valueOf(it) }.getOrNull() }
                ?: SuccessKind.LOGIN

            val title: String = stringResource(
                when (kind) {
                    SuccessKind.LOGIN -> R.string.session_started
                    SuccessKind.REGISTER -> R.string.user_registered
                },
            )
            val actionLabel: String = stringResource(
                when (kind) {
                    SuccessKind.LOGIN -> R.string.main_menu
                    SuccessKind.REGISTER -> R.string.login_button
                },
            )

            AuthSuccessScreen(
                title = title,
                actionLabel = actionLabel,
                onAction = {
                    when (kind) {
                        SuccessKind.LOGIN -> onAuthComplete()
                        SuccessKind.REGISTER -> {
                            navController.popBackStack(route = AuthRoutes.LOGIN, inclusive = false)
                        }
                    }
                },
            )
        }
    }
}
