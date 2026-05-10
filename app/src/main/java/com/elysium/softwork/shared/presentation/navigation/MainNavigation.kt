package com.elysium.softwork.shared.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elysium.softwork.R
import com.elysium.softwork.shared.presentation.views.home.HomeScreen
import com.elysium.softwork.shared.presentation.views.identity.ProtectedIdentityScreen
import com.elysium.softwork.shared.presentation.views.profile.ProfileScreen
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/** Bottom-nav destination definition. Used both to render the bar and to declare routes. */
private data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
    val contentDescriptionRes: Int,
)

/** Routes for the authenticated shell. */
object MainRoutes {
    const val HOME: String = "main/home"
    const val PROFILE: String = "main/profile"
    const val FORUM: String = "main/forum"
    const val NOTIFICATIONS: String = "main/notifications"
    const val PROTECTED_IDENTITY: String = "main/protected-identity"
}

private val BottomDestinations: List<BottomDestination> = listOf(
    BottomDestination(MainRoutes.HOME, R.string.nav_home, R.drawable.ic_home, R.string.cd_home),
    BottomDestination(MainRoutes.PROFILE, R.string.nav_profile, R.drawable.ic_person, R.string.cd_user_icon),
    BottomDestination(MainRoutes.FORUM, R.string.nav_forum, R.drawable.ic_forum, R.string.cd_forum),
    BottomDestination(MainRoutes.NOTIFICATIONS, R.string.nav_notifications, R.drawable.ic_notifications, R.string.cd_notifications),
)

/**
 * Authenticated shell. Owns the bottom navigation bar and nests a [NavHost] for the four
 * tabs. The forum and notifications tabs render placeholders until those bounded contexts
 * come online.
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
                    onReportIncident = { /* Phase 5: incident flow not yet implemented. */ },
                    onOpenForums = { navController.navigateToTab(MainRoutes.FORUM) },
                    onOpenAssistant = { /* Phase 6: AI assistant not yet implemented. */ },
                )
            }
            composable(MainRoutes.PROFILE) {
                ProfileScreen(
                    onEditProfile = { /* Phase 3: edit-profile flow not yet implemented. */ },
                    onLogout = onLogout,
                    onOpenAnonymousForumSettings = {
                        navController.navigate(MainRoutes.PROTECTED_IDENTITY)
                    },
                    onOpenPaymentMethods = { /* Phase 3: payment methods not yet implemented. */ },
                )
            }
            composable(MainRoutes.PROTECTED_IDENTITY) {
                ProtectedIdentityScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(MainRoutes.FORUM) {
                PlaceholderScreen(
                    title = stringResource(R.string.placeholder_forum_title),
                    subtitle = stringResource(R.string.placeholder_forum_subtitle),
                )
            }
            composable(MainRoutes.NOTIFICATIONS) {
                PlaceholderScreen(
                    title = stringResource(R.string.placeholder_notifications_title),
                    subtitle = stringResource(R.string.placeholder_notifications_subtitle),
                )
            }
        }
    }
}

@Composable
private fun SoftWorkBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = backStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp,
    ) {
        BottomDestinations.forEach { destination ->
            val selected: Boolean = backStackEntry?.destination?.hierarchy?.any { it.route == destination.route } == true ||
                currentRoute == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(destination.route) },
                icon = {
                    Icon(
                        painter = painterResource(destination.iconRes),
                        contentDescription = stringResource(destination.contentDescriptionRes),
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimarySky,
                    selectedTextColor = PrimarySky,
                    unselectedIconColor = AccentMint,
                    unselectedTextColor = AccentMint,
                    indicatorColor = Color.Transparent,
                ),
            )
        }
    }
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

@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryNavy,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
            )
        }
    }
}
