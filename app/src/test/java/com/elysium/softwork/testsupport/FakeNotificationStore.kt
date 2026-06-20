package com.elysium.softwork.testsupport

import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.notifications.domain.model.NotificationDetail

/**
 * In-memory test double for [NotificationStore].
 *
 * Deterministic: each read returns the value programmed via the public `next*` properties,
 * so tests can drive both the happy path (seeded notifications + details that the use case
 * joins) and the failure path (a programmed [Result.failure], e.g. a `BadRequestException`).
 *
 * @property nextNotificationsResult value returned by the next [getNotifications] call.
 * @property nextDetailsResult value returned by the next [getNotificationDetails] call.
 */
open class FakeNotificationStore(
    var nextNotificationsResult: Result<List<Notification>> = Result.success(emptyList()),
    var nextDetailsResult: Result<List<NotificationDetail>> = Result.success(emptyList()),
) : NotificationStore {

    override suspend fun getNotifications(): Result<List<Notification>> = nextNotificationsResult

    override suspend fun getNotificationDetails(): Result<List<NotificationDetail>> = nextDetailsResult
}
