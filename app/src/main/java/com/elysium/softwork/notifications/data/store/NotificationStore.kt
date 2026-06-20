package com.elysium.softwork.notifications.data.store

import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.notifications.domain.model.NotificationDetail

/**
 * Notifications data port.
 *
 * Exposes the two read operations the feed needs — the notification records and their
 * details — as suspending functions returning [Result] so callers get a single error
 * channel; a `400 Bad Request` surfaces as a
 * [com.elysium.softwork.shared.data.network.BadRequestException]. The application layer joins
 * the two into the render-ready feed.
 */
interface NotificationStore {

    /** Fetches the notification records (`GET /api/v1/notifications`). */
    suspend fun getNotifications(): Result<List<Notification>>

    /** Fetches the notification details (`GET /api/v1/notification-details`). */
    suspend fun getNotificationDetails(): Result<List<NotificationDetail>>
}
