package com.elysium.softwork.notifications.presentation.viewmodel

import com.elysium.softwork.notifications.application.usecase.GetNotificationsUseCase
import com.elysium.softwork.notifications.domain.model.Notification
import com.elysium.softwork.shared.utils.values.NotificationType
import com.elysium.softwork.testsupport.FakeNotificationStore
import com.elysium.softwork.testsupport.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [NotificationsViewModel].
 *
 * The ViewModel subscribes to [com.elysium.softwork.notifications.data.store.NotificationStore.getNotifications]
 * inside `viewModelScope` on construction and surfaces the latest snapshot through
 * `notifications`. The rule installs an `UnconfinedTestDispatcher` so the init-time
 * collector runs eagerly and the first emission is observable immediately after the
 * VM is instantiated.
 *
 * Behaviors under test:
 *  - The first store snapshot reaches the ViewModel on construction.
 *  - Subsequent store emissions update `notifications` reactively.
 *  - List items maintain stable identity (`id` unchanged → same reference if the
 *    backing list re-emits an unchanged entry).
 *  - Type discriminators round-trip without mutation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    /**
     * Builds the ViewModel with the real application-layer use case wrapping the fake
     * store, mirroring the production factory wiring without touching the locator.
     */
    private fun newViewModel(store: FakeNotificationStore): NotificationsViewModel =
        NotificationsViewModel(getNotifications = GetNotificationsUseCase(store))

    private fun sampleFeed(): List<Notification> = listOf(
        Notification(
            id = "n-1",
            type = NotificationType.SURVEY,
            title = "New survey available",
            description = "Climate Q4 — 2 minutes",
        ),
        Notification(
            id = "n-2",
            type = NotificationType.PAYMENT,
            title = "Plan renewal",
            description = "Expires in 3 days",
        ),
        Notification(
            id = "n-3",
            type = NotificationType.FORUM,
            title = "Reply on your post",
            description = "Coffee machine broken",
        ),
    )

    @Test
    fun `init exposes the first store snapshot`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = FakeNotificationStore(initial = sampleFeed())
        val vm = newViewModel(store)
        advanceUntilIdle()

        assertEquals(3, vm.notifications.value.size)
        assertEquals("n-1", vm.notifications.value[0].id)
        assertEquals(NotificationType.SURVEY, vm.notifications.value[0].type)
    }

    @Test
    fun `subsequent store emissions update notifications`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeNotificationStore()
            val vm = newViewModel(store)
            advanceUntilIdle()
            assertEquals(emptyList<Any>(), vm.notifications.value)

            store.emit(sampleFeed())
            advanceUntilIdle()

            assertEquals(3, vm.notifications.value.size)
        }

    @Test
    fun `list items keep stable identity across emissions`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initial = Notification(
                id = "n-1",
                type = NotificationType.MESSAGE,
                title = "Welcome",
                description = "Hello",
            )
            val store = FakeNotificationStore(initial = listOf(initial))
            val vm = newViewModel(store)
            advanceUntilIdle()

            val firstSnapshot = vm.notifications.value
            val newer = Notification(
                id = "n-2",
                type = NotificationType.FORUM,
                title = "Reply",
                description = "Body",
            )
            store.emit(listOf(initial, newer))
            advanceUntilIdle()

            val secondSnapshot = vm.notifications.value
            assertEquals(2, secondSnapshot.size)
            // The previously emitted notification keeps the exact same instance reference
            // because the FakeNotificationStore re-emits the unchanged entry, so list
            // diffing in the UI layer can short-circuit.
            assertSame(firstSnapshot[0], secondSnapshot[0])
        }

    @Test
    fun `type discriminator survives the round trip`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeNotificationStore(initial = sampleFeed())
            val vm = newViewModel(store)
            advanceUntilIdle()

            val typesByOrder = vm.notifications.value.map { it.type }
            assertEquals(
                listOf(NotificationType.SURVEY, NotificationType.PAYMENT, NotificationType.FORUM),
                typesByOrder,
            )
        }
}
