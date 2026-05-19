package com.elysium.softwork.notifications.domain.model

import com.elysium.softwork.shared.utils.values.NotificationType
import com.google.gson.annotations.SerializedName

/**
 * An in-app notification surfaced on the Notifications tab.
 *
 * Phase 6 ships a mocked list; once the `/notifications` endpoint exists this class doubles
 * as the wire bean per the project's pragmatic shortcut (see CLAUDE.md → "Bean / Pragmatic
 * Shortcut"). [type] drives the per-card color theming on `NotificationsScreen`.
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
