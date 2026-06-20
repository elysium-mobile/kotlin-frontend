package com.elysium.softwork.notifications.presentation.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.notifications.presentation.viewmodel.NotificationsViewModel
import com.elysium.softwork.notifications.domain.model.NotificationFeedItem
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal
import com.elysium.softwork.shared.presentation.theme.Warning
import com.elysium.softwork.shared.utils.values.NotificationType

/**
 * Notifications Screen — color-coded list of the worker's in-app notifications.
 *
 * Per the design brief, this screen intentionally deviates from the plain monochrome
 * mockup: each card is themed by [NotificationType] (background tint + icon/title color)
 * to make the list more vibrant while still respecting the brand palette tokens declared
 * in `shared/presentation/theme/Color.kt`.
 *
 * @param onNotificationClick invoked when the user taps a card; the backend `notification_id`
 *   is forwarded so the host can route to the per-type deep-link target.
 */
@Composable
fun NotificationsScreen(
    onNotificationClick: (Long) -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory),
) {
    val notifications: List<NotificationFeedItem> by viewModel.notifications.collectAsStateWithLifecycle()
    val errorMessage: String? by viewModel.errorMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite),
    ) {
        NotificationsHeader()

        errorMessage?.let { message ->
            Text(
                text = message,
                color = Danger,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = notifications, key = { it.id }) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification.id) },
                )
            }
        }
    }
}

@Composable
private fun NotificationsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.notifications_title),
            color = PrimaryNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationFeedItem,
    onClick: () -> Unit,
) {
    val theme: NotificationTheme = NotificationTheme.forType(notification.type)

    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(theme = theme)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = notification.title,
                    color = theme.foreground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = notification.content,
                    color = AccentDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = PrimarySky,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategoryIcon(theme: NotificationTheme) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(theme.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(theme.iconRes),
            contentDescription = null,
            tint = theme.foreground,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Per-category color + glyph bundle used by [NotificationCard]. Keeping the mapping in one
 * place — rather than scattering `when (type)` blocks across the composables — makes it
 * easy to retune the palette without grepping the file.
 *
 * The "soft surface" backgrounds (sky/warning/navy tints) are intentionally declared inline
 * here rather than promoted to `Color.kt`: they only exist for this screen, and shipping
 * them as semantic tokens would suggest a broader contract than we want.
 */
@Immutable
private data class NotificationTheme(
    val background: Color,
    val foreground: Color,
    val iconRes: Int,
) {
    companion object {
        // Soft surface tints — see the KDoc above on why these don't live in Color.kt.
        private val WarningSurface: Color = Color(0xFFFFF9F0)
        private val SkySurface: Color = Color(0xFFF0F8FF)
        private val NavySurface: Color = Color(0xFFF0F4F8)

        fun forType(type: NotificationType): NotificationTheme = when (type) {
            NotificationType.SURVEY -> NotificationTheme(
                background = AccentMint,
                foreground = PrimaryTeal,
                iconRes = R.drawable.ic_check_circle,
            )
            NotificationType.PAYMENT -> NotificationTheme(
                background = WarningSurface,
                foreground = Warning,
                iconRes = R.drawable.ic_payments,
            )
            NotificationType.FORUM -> NotificationTheme(
                background = SkySurface,
                foreground = PrimarySky,
                iconRes = R.drawable.ic_forum,
            )
            NotificationType.MESSAGE -> NotificationTheme(
                background = NavySurface,
                foreground = PrimaryNavy,
                iconRes = R.drawable.ic_send,
            )
        }
    }
}
