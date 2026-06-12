package com.elysium.softwork.notifications.application.usecase

import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/**
 * Streams the worker's in-app notification feed.
 *
 * Stateless; safe to share a single instance process-wide. Mutation operations
 * (mark-as-read, clear-all) become sibling use cases when the data port grows them.
 *
 * @param store notifications data port.
 */
class GetNotificationsUseCase(private val store: NotificationStore) {

    /** @return flow of the notification feed, re-emitting on every change. */
    operator fun invoke(): Flow<List<Notification>> = store.getNotifications()
}
