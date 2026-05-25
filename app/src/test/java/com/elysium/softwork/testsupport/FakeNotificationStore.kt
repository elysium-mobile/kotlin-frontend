package com.elysium.softwork.testsupport

import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory test double for [NotificationStore].
 *
 * Backs [getNotifications] with a [MutableStateFlow] so tests can drive the feed
 * through [emit]. Subscribers see the current snapshot synchronously on first
 * collection and every subsequent mutation as a new emission, mirroring the contract
 * of the production Retrofit-backed implementation that will replace it.
 */
open class FakeNotificationStore(
    initial: List<Notification> = emptyList(),
) : NotificationStore {

    private val _notifications: MutableStateFlow<List<Notification>> = MutableStateFlow(initial)

    override fun getNotifications(): Flow<List<Notification>> = _notifications

    /** Test-only emission helper. Replaces the list seen by subscribers. */
    fun emit(list: List<Notification>) {
        _notifications.value = list
    }
}
