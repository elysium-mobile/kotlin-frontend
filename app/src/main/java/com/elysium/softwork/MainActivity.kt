package com.elysium.softwork

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.elysium.softwork.iam.presentation.navigation.AuthNavHost
import com.elysium.softwork.payment.membership.presentation.navigation.PaymentRoutes
import com.elysium.softwork.payment.membership.presentation.navigation.paymentGraph
import com.elysium.softwork.shared.presentation.navigation.MainNavHost
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme

/**
 * Single Activity hosting the entire Compose UI tree.
 *
 * Inherits from [AppCompatActivity] so that [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales]
 * automatically recreates the activity with the new configuration on API 29-32 (the AppCompat
 * back-port path). On API 33+ the platform `LocaleManager` handles recreation transparently;
 * the AppCompat dependency is harmless on those versions.
 *
 * The Activity owns two booleans — "is the user authenticated" and "does the user have an
 * active membership" — and routes the worker into one of three top-level hosts:
 * - **`AuthNavHost`** when unauthenticated.
 * - **`PaymentOnboardingHost`** when authenticated but `hasMembership` is `false` (the gate).
 *   The graph is rooted at [PaymentRoutes.SELECTION] and lets the worker pick a plan, enter
 *   a card, and pay. Tapping "Main menu" on the success screen flips the flag.
 * - **[MainNavHost]** when authenticated AND a membership is active.
 *
 * The `hasMembership` flag is collected reactively from `MembershipStore.hasMembership`,
 * which means that "Cancel subscription" from inside the main shell immediately unmounts
 * the shell, wipes its back stack (the whole `NavHost` is recreated), and drops the worker
 * back at the membership selection screen.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge BEFORE setContent so the system bars become transparent and
        // the Compose tree owns the entire window. Window-inset padding is then applied
        // explicitly at the relevant call sites (e.g. the bottom navigation bar) using
        // Modifier.windowInsetsPadding, which keeps interactive surfaces clear of the
        // status bar, the navigation bar, and the gesture pill on any device form factor.
        enableEdgeToEdge()
        setContent {
            SoftWorkTheme {
                val app = remember { application as SoftWorkApplication }
                val locator = app.serviceLocator

                var isAuthenticated: Boolean by rememberSaveable {
                    mutableStateOf(locator.authStore.activeToken() != null)
                }
                val hasMembership: Boolean by locator.membershipStore.hasMembership.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when {
                        !isAuthenticated -> AuthNavHost(
                            onAuthComplete = { isAuthenticated = true },
                        )
                        !hasMembership -> PaymentOnboardingHost()
                        else -> MainNavHost(
                            userName = stringResource(R.string.home_user_name_placeholder),
                            onLogout = {
                                locator.authStore.clearSession()
                                isAuthenticated = false
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standalone payment NavHost mounted when an authenticated worker lacks an active
 * membership. Lives in its own composable so the back stack belongs exclusively to the
 * onboarding flow — when the worker pays successfully, the store's `hasMembership` flag
 * flips to `true`, [MainActivity] recomposes and unmounts this host entirely, which
 * naturally clears the back stack without any explicit `popUpTo`.
 *
 * The success screen's "Main menu" CTA does not need to navigate — flipping the gate flag
 * already triggers the host swap. The lambda passed to `paymentGraph` is therefore a no-op
 * on the onboarding mount.
 */
@androidx.compose.runtime.Composable
private fun PaymentOnboardingHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PaymentRoutes.SELECTION,
        modifier = Modifier.fillMaxSize(),
    ) {
        paymentGraph(
            navController = navController,
            onExitToMainShell = { /* no-op: gate flip in the VM drives the swap. */ },
        )
    }
}
