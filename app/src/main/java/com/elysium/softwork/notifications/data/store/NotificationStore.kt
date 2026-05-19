package com.elysium.softwork.notifications.data.store

import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Notifications data port. Phase 6 only models the read-side feed; future milestones will
 * add `markAsRead(id)` / `clearAll()` mutations once the backend contract is defined.
 */
interface NotificationStore {

    /**
     * Observes the user's in-app notifications. The mocked implementation emits a static
     * four-entry catalogue; a future Retrofit-backed implementation keeps this shape and
     * re-emits when a refresh or push lands.
     */
    fun getNotifications(): Flow<List<Notification>>
}
