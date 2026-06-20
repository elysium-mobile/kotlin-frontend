package com.elysium.softwork.notifications.domain.model

/**
 * The human-readable payload of a [Notification] — the annotation-free bean for the
 * `notification-details` endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Property names mirror the backend wire keys exactly so Gson maps them by reflection
 * without `@SerializedName`. The backend's mixed naming returns snake_case while the create
 * request expects camelCase for the parent link, so both spellings coexist as nullable
 * fields:
 * - **parent notification**: request key `notificationId`, response key `notification_id`.
 *
 * @property notification_detail_id primary key of the detail row.
 * @property title short headline rendered on the notification card.
 * @property content one-line body of the notification.
 * @property notificationId parent link on the **create request** (`notificationId`).
 * @property notification_id parent link on the **response** (`notification_id`); used to join
 *   the detail to its owning [Notification].
 */
data class NotificationDetail(
    val notification_detail_id: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val notificationId: Long? = null,
    val notification_id: Long? = null,
)
