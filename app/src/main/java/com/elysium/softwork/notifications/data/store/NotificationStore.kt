package com.elysium.softwork.notifications.data.store

import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Notifications data port. Currently models the read-side feed only; mutation endpoints
 * such as `markAsRead(id)` / `clearAll()` can be appended to this contract without
 * changing call sites.
 */
interface NotificationStore {

    /**
     * Observes the user's in-app notifications. The mocked implementation emits a static
     * four-entry catalogue; a future Retrofit-backed implementation keeps this shape and
     * re-emits when a refresh or push lands.
     */
    fun getNotifications(): Flow<List<Notification>>
}
