package com.elysium.softwork.notifications.domain.model

import com.elysium.softwork.shared.utils.values.NotificationType

/**
 * An in-app notification surfaced on the Notifications tab.
 *
 * Immutable domain entity, framework-agnostic by design: property names match the backend
 * wire keys exactly so the data layer's JSON serializer resolves them by reflection
 * without mapping annotations. [type] drives the per-card color theming on the
 * notifications feed.
 *
 * @property id stable identifier issued by the backend (or a mock literal in the seed set).
 * @property type category discriminator — see [NotificationType].
 * @property title short headline (already localized by the producer).
 * @property description one-line body of the notification.
 * @property isRead `true` once the user has opened or dismissed the notification.
 */
data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val title: String = "",
    val description: String = "",
    val isRead: Boolean = false,
)
