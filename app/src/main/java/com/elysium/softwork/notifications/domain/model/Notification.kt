package com.elysium.softwork.notifications.domain.model

import com.elysium.softwork.shared.utils.values.NotificationType
import com.google.gson.annotations.SerializedName

/**
 * An in-app notification surfaced on the Notifications tab.
 *
 * The same instance flows through the Retrofit web service request/response and into the
 * in-memory feed, so all fields default to nullable-friendly empty values. [type] drives
 * the per-card color theming on the notifications feed.
 *
 * @property id stable identifier issued by the backend (or a mock literal in the seed set).
 * @property type category discriminator — see [NotificationType].
 * @property title short headline (already localized by the producer).
 * @property description one-line body of the notification.
 * @property isRead `true` once the user has opened or dismissed the notification.
 */
data class Notification(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: NotificationType = NotificationType.MESSAGE,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("isRead") val isRead: Boolean = false,
)
