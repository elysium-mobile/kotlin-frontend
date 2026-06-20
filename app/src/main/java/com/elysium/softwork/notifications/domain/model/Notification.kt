package com.elysium.softwork.notifications.domain.model

/**
 * A backend notification record — the annotation-free bean for the `notifications` endpoints
 * (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. This record carries only the **category + ownership** metadata;
 * the human-readable headline/body live in a sibling [NotificationDetail] linked by
 * [notification_id]. The two are joined into a [NotificationFeedItem] for rendering.
 *
 * @property notification_id primary key; also the foreign key a [NotificationDetail] points at.
 * @property seen `true` once the worker has opened the notification.
 * @property notification_type category discriminator as a raw wire string (e.g. `survey`,
 *   `payment`); resolved to [com.elysium.softwork.shared.utils.values.NotificationType] for
 *   card theming.
 * @property user_account_id owning account; used to filter the feed to the signed-in worker.
 */
data class Notification(
    val notification_id: Long? = null,
    val seen: Boolean? = null,
    val notification_type: String? = null,
    val user_account_id: Long? = null,
)
