package com.elysium.softwork.notifications.presentation.viewmodel

import com.elysium.softwork.notifications.application.usecase.GetNotificationsUseCase
import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.notifications.domain.model.NotificationDetail
import com.elysium.softwork.shared.data.network.BadRequestException
import com.elysium.softwork.shared.data.network.BadRequestResponse
import com.elysium.softwork.shared.utils.values.NotificationType
import com.elysium.softwork.testsupport.FakeNotificationStore
import com.elysium.softwork.testsupport.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [NotificationsViewModel].
 *
 * The ViewModel loads the feed through [GetNotificationsUseCase] inside `viewModelScope` on
 * construction. The use case joins the notification records with their details and resolves
 * the wire category into a [NotificationType]; the rule installs an `UnconfinedTestDispatcher`
 * so the init-time load runs eagerly and the result is observable right after construction.
 *
 * Behaviors under test:
 *  - notifications + details are joined into the render-ready feed, with the category resolved.
 *  - an unknown wire category falls back to [NotificationType.MESSAGE].
 *  - the `user_account_id` provider filters the feed to the signed-in worker.
 *  - a `400 Bad Request` routes its `field_errors` message into `errorMessage`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private fun newViewModel(
        store: FakeNotificationStore,
        accountId: Long? = null,
    ): NotificationsViewModel =
        NotificationsViewModel(
            getNotifications = GetNotificationsUseCase(store, accountIdProvider = { accountId }),
        )

    private fun sampleNotifications(): List<Notification> = listOf(
        Notification(notification_id = 1, seen = false, notification_type = "survey", user_account_id = 7),
        Notification(notification_id = 2, seen = true, notification_type = "PAYMENT", user_account_id = 7),
        Notification(notification_id = 3, seen = false, notification_type = "unknown_kind", user_account_id = 9),
    )

    private fun sampleDetails(): List<NotificationDetail> = listOf(
        NotificationDetail(notification_detail_id = 10, title = "New survey", content = "Climate Q4", notification_id = 1),
        NotificationDetail(notification_detail_id = 11, title = "Plan renewal", content = "Expires soon", notification_id = 2),
    )

    @Test
    fun `init joins notifications with details and resolves the category`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeNotificationStore(
                nextNotificationsResult = Result.success(sampleNotifications()),
                nextDetailsResult = Result.success(sampleDetails()),
            )
            val vm = newViewModel(store)
            advanceUntilIdle()

            val feed = vm.notifications.value
            assertEquals(3, feed.size)
            assertEquals(1L, feed[0].id)
            assertEquals(NotificationType.SURVEY, feed[0].type)
            assertEquals("New survey", feed[0].title)
            assertEquals("Climate Q4", feed[0].content)
            // Case-insensitive category resolution from the wire string.
            assertEquals(NotificationType.PAYMENT, feed[1].type)
            assertNull(vm.errorMessage.value)
        }

    @Test
    fun `unknown wire category falls back to MESSAGE`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeNotificationStore(
                nextNotificationsResult = Result.success(sampleNotifications()),
                nextDetailsResult = Result.success(sampleDetails()),
            )
            val vm = newViewModel(store)
            advanceUntilIdle()

            // notification_id = 3 carries an unrecognized category and has no detail.
            val third = vm.notifications.value.first { it.id == 3L }
            assertEquals(NotificationType.MESSAGE, third.type)
            assertEquals("", third.title)
        }

    @Test
    fun `account id provider filters the feed to the signed-in worker`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeNotificationStore(
                nextNotificationsResult = Result.success(sampleNotifications()),
                nextDetailsResult = Result.success(sampleDetails()),
            )
            val vm = newViewModel(store, accountId = 7L)
            advanceUntilIdle()

            val feed = vm.notifications.value
            assertEquals(2, feed.size)
            assertTrue(feed.all { it.id == 1L || it.id == 2L })
        }

    @Test
    fun `400 routes the field error into errorMessage`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val badRequest = BadRequestException(
                BadRequestResponse(
                    message = "Internal validation failed",
                    field_errors = mapOf("argument" to "user_account_id must be provided"),
                ),
            )
            val store = FakeNotificationStore(nextNotificationsResult = Result.failure(badRequest))
            val vm = newViewModel(store)
            advanceUntilIdle()

            assertEquals("user_account_id must be provided", vm.errorMessage.value)
            assertTrue(vm.notifications.value.isEmpty())
        }
}
