package com.elysium.softwork.notifications.domain.model

import com.elysium.softwork.shared.utils.values.NotificationType

/**
 * A render-ready notification: the join of a [Notification] (category + state) and its
 * [NotificationDetail] (title + content), with the wire category resolved into a strongly
 * typed [NotificationType].
 *
 * This is a domain **aggregate**, not a wire bean — it never travels over the network, so it
 * is not subject to the bean shortcut. The application layer assembles it; the presentation
 * layer renders it (theming the card by [type]).
 *
 * @property id the backend `notification_id` (stable list key).
 * @property type resolved category driving the card colour/icon; falls back to
 *   [NotificationType.MESSAGE] when the wire string is unknown.
 * @property title headline sourced from the linked detail (empty when no detail exists).
 * @property content body sourced from the linked detail (empty when no detail exists).
 * @property seen `true` once the worker has opened the notification.
 */
data class NotificationFeedItem(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val content: String,
    val seen: Boolean,
)
