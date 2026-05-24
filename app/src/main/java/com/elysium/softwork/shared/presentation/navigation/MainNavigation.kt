package com.elysium.softwork.shared.presentation.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elysium.softwork.R
import com.elysium.softwork.feedback.presentation.navigation.FeedbackRoutes
import com.elysium.softwork.feedback.presentation.navigation.feedbackGraph
import com.elysium.softwork.notifications.presentation.navigation.NotificationRoutes
import com.elysium.softwork.notifications.presentation.navigation.notificationGraph
import com.elysium.softwork.payment.membership.presentation.navigation.PaymentRoutes
import com.elysium.softwork.payment.membership.presentation.navigation.paymentGraph
import com.elysium.softwork.worker.forum.presentation.navigation.ForumRoutes
import com.elysium.softwork.worker.forum.presentation.navigation.forumGraph
import com.elysium.softwork.shared.presentation.views.home.HomeScreen
import com.elysium.softwork.shared.presentation.views.identity.ProtectedIdentityScreen
import com.elysium.softwork.shared.presentation.views.profile.ProfileScreen
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/** Bottom-nav destination definition. Used both to render the bar and to declare routes. */
private data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
    val contentDescriptionRes: Int,
)

private val BottomDestinations: List<BottomDestination> = listOf(
    BottomDestination(MainRoutes.HOME, R.string.nav_home, R.drawable.ic_home, R.string.cd_home),
    BottomDestination(ForumRoutes.FEED, R.string.nav_forum, R.drawable.ic_forum, R.string.cd_forum),
    BottomDestination(NotificationRoutes.NOTIFICATIONS_FEED, R.string.nav_notifications, R.drawable.ic_notifications, R.string.cd_notifications),
    BottomDestination(MainRoutes.PROFILE, R.string.nav_profile, R.drawable.ic_person, R.string.cd_user_icon),
)

/**
 * Authenticated shell. Owns the bottom navigation bar and nests a [NavHost] for the four
 * tabs (Home, Forum, Notifications, Profile) plus the auxiliary destinations reached from
 * Home action cards and Profile rows.
 *
 * @param userName name passed straight to [HomeScreen] for the greeting block.
 * @param onLogout invoked when the user taps "Sign out" on the profile screen. The
 *   hosting Activity owns the session-clearing + back-to-IAM transition.
 */
@Composable
fun MainNavHost(
    userName: String,
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    Scaffold(
        bottomBar = { SoftWorkBottomBar(navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(MainRoutes.HOME) {
                HomeScreen(
                    userName = userName,
                    onReportIncident = {
                        // Home doesn't have a post selected, so filing a new report from
                        // here doesn't fit the post-scoped ForumReportScreen contract.
                        // Instead, we surface the read-only status list — users file new
                        // reports from a thread via the per-post "Report" affordance.
                        navController.navigate(ForumRoutes.REPORTS_STATUS)
                    },
                    onOpenForums = { navController.navigateToTab(ForumRoutes.FEED) },
                    onOpenAssistant = { /* AI assistant entry point is not yet implemented. */ },
                    onOpenSurveys = { navController.navigate(FeedbackRoutes.PENDING_SURVEYS) },
                    onOpenMembership = {
                        navController.navigate(
                            PaymentRoutes.methods(
                                planKey = PaymentRoutes.CURRENT_PLAN_SENTINEL,
                                fromSettings = true,
                            ),
                        )
                    },
                )
            }
            composable(MainRoutes.PROFILE) {
                ProfileScreen(
                    onEditProfile = { /* Edit-profile flow is not yet implemented. */ },
                    onLogout = onLogout,
                    onOpenAnonymousForumSettings = {
                        navController.navigate(MainRoutes.PROTECTED_IDENTITY)
                    },
                    onOpenPaymentMethods = {
                        // Settings entry into the payment graph. Pass the sentinel so the
                        // methods screen resolves the active plan from the store, and flip
                        // fromSettings = true so the "Cancel subscription" row renders.
                        navController.navigate(
                            PaymentRoutes.methods(
                                planKey = PaymentRoutes.CURRENT_PLAN_SENTINEL,
                                fromSettings = true,
                            ),
                        )
                    },
                )
            }
            composable(MainRoutes.PROTECTED_IDENTITY) {
                ProtectedIdentityScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            feedbackGraph(navController = navController)
            forumGraph(navController = navController, userName = userName)
            notificationGraph(navController = navController)
            // Payment routes mounted here so Profile → "Payment methods" stays on the
            // main back stack. Cancellation does not need to navigate — flipping the
            // membership flag in the store causes MainActivity to swap the host entirely.
            paymentGraph(navController = navController, onExitToMainShell = NoOpExit)
        }
    }
}

/** Hoisted no-op for the settings mount of `paymentGraph` to avoid per-recomposition allocation. */
private val NoOpExit: () -> Unit = {}

/**
 * Bottom navigation bar for the authenticated shell.
 *
 * Window-inset handling is explicit:
 * - `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` reserves exact pixel space
 *   for whichever system navigation affordance the device renders — the three-button bar
 *   on legacy devices, the gesture pill on modern devices, or a zero-height inset on
 *   devices that hide system navigation entirely.
 * - `windowInsets = WindowInsets(0)` disables [NavigationBar]'s internal default insets so
 *   the explicit modifier above is the single source of truth, preventing double padding.
 *
 * The 72.dp height is the interactive item region; the inset padding is rendered as an
 * additional band underneath, so the total bar height grows automatically to clear the
 * system navigation without overlapping it. Because the bar is hosted in the `bottomBar`
 * slot of [Scaffold], the resulting height is propagated to the content `PaddingValues`,
 * keeping screen bodies clear of the bar.
 *
 * `alwaysShowLabel = false` on each [NavigationBarItem] hides the label while unselected,
 * recovering vertical real estate on narrower screens and reducing visual noise.
 */
@Composable
private fun SoftWorkBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Walk the destination hierarchy exactly once per nav event and snapshot every active
    // route into a Set so the per-item selected check below is O(1) instead of an O(depth)
    // traversal repeated for each destination.
    val activeRoutes: Set<String> = remember(backStackEntry) {
        backStackEntry?.destination?.hierarchy
            ?.mapNotNull { it.route }
            ?.toSet()
            .orEmpty()
    }

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = PrimarySky,
        selectedTextColor = PrimarySky,
        unselectedIconColor = AccentMint,
        unselectedTextColor = AccentMint,
        indicatorColor = Color.Transparent,
    )

    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(72.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0),
    ) {
        BottomDestinations.forEach { destination ->
            BottomNavItem(
                destination = destination,
                selected = destination.route in activeRoutes,
                navController = navController,
                colors = itemColors,
            )
        }
    }
}

/**
 * One bottom-bar entry. Extracted from [SoftWorkBottomBar] so the per-item slot lambdas
 * (`icon`, `label`, `onClick`) can be hoisted by [remember] keyed on the stable
 * [destination] reference — preventing fresh lambda allocations on every recomposition
 * of the bar and giving Compose's smart-skipping a chance to bypass unchanged items.
 */
@Composable
private fun RowScope.BottomNavItem(
    destination: BottomDestination,
    selected: Boolean,
    navController: NavHostController,
    colors: NavigationBarItemColors,
) {
    val onClick = remember(destination.route, navController) {
        { navController.navigateToTab(destination.route) }
    }
    val icon: @Composable () -> Unit = remember(destination) {
        {
            Icon(
                painter = painterResource(destination.iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
    val label: @Composable () -> Unit = remember(destination) {
        {
            Text(
                text = stringResource(destination.labelRes),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = label,
        alwaysShowLabel = false,
        colors = colors,
    )
}

/**
 * Tab-style navigation: pop to the start, single-top, restore state. Keeps the back stack
 * shallow so the system back button always exits the shell rather than cycling tabs.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

