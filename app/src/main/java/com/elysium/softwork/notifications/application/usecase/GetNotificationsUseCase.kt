package com.elysium.softwork.notifications.application.usecase

import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.domain.model.NotificationFeedItem
import com.elysium.softwork.shared.utils.values.NotificationType

/**
 * Builds the worker's notification feed from the live backend.
 *
 * Owns the query business rules:
 * - **dynamically appends the authenticated `user_account_id`** via [accountIdProvider]
 *   (wired by the ViewModel factory to read `KEY_USER_ACCOUNT_ID` from `SharedPrefsManager`)
 *   and keeps only the records owned by the signed-in worker;
 * - joins each [com.elysium.softwork.notifications.domain.model.Notification] with its
 *   [com.elysium.softwork.notifications.domain.model.NotificationDetail] (matched by
 *   `notification_id`) and resolves the wire category into a [NotificationType];
 * - tolerates unknown categories by falling back to [NotificationType.MESSAGE].
 *
 * The provider is read **per invocation** so a re-login (new account id) is reflected on the
 * next refresh. Stateless; safe to share a single instance process-wide.
 *
 * @param store notifications data port.
 * @param accountIdProvider supplies the signed-in `user_account_id` (or `null` when absent),
 *   resourced from `SharedPrefsManager`.
 */
class GetNotificationsUseCase(
    private val store: NotificationStore,
    private val accountIdProvider: () -> Long?,
) {

    /**
     * Fetches, filters, and joins the feed.
     *
     * @return [Result.success] with the render-ready feed or [Result.failure] (a `400` arrives
     *   as a [com.elysium.softwork.shared.data.network.BadRequestException]).
     */
    suspend operator fun invoke(): Result<List<NotificationFeedItem>> = runCatching {
        val accountId: Long? = accountIdProvider()

        val notifications = store.getNotifications().getOrThrow()
            .let { all -> if (accountId != null) all.filter { it.user_account_id == accountId } else all }
        val details = store.getNotificationDetails().getOrThrow()

        notifications.mapNotNull { notification ->
            val id: Long = notification.notification_id ?: return@mapNotNull null
            val detail = details.firstOrNull { it.notification_id == id }
            NotificationFeedItem(
                id = id,
                type = NotificationType.fromKey(notification.notification_type) ?: NotificationType.MESSAGE,
                title = detail?.title.orEmpty(),
                content = detail?.content.orEmpty(),
                seen = notification.seen ?: false,
            )
        }
    }
}
