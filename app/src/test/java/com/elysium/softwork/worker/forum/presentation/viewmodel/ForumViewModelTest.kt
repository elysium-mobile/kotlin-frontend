package com.elysium.softwork.worker.forum.presentation.viewmodel

import com.elysium.softwork.testsupport.FakeForumStore
import com.elysium.softwork.testsupport.MainDispatcherRule
import com.elysium.softwork.worker.forum.application.usecase.ObserveThreadsUseCase
import com.elysium.softwork.worker.forum.application.usecase.RefreshThreadsUseCase
import com.elysium.softwork.worker.forum.domain.model.Thread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ForumViewModel].
 *
 * [ForumViewModel.threads] is a `stateIn(WhileSubscribed)` flow, so the upstream
 * [com.elysium.softwork.worker.forum.data.store.ForumStore.observeThreads] only collects
 * while there is an active subscriber. Tests launch a no-op collector in `backgroundScope`
 * so the upstream activates and `threads.value` mirrors the latest [FakeForumStore] emission.
 *
 * The dispatcher rule installs an `UnconfinedTestDispatcher` so the `init`-time
 * `viewModelScope.launch { refreshThreads() }` runs eagerly.
 *
 * Behaviors under test:
 *  - `init` calls `refresh()` exactly once.
 *  - `threads` re-emits when the store backing flow emits a fresh list.
 *  - List items keep stable identity across emissions (same instance re-emitted).
 *  - `refresh()` forwards to the store an additional time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private fun newViewModel(store: FakeForumStore): ForumViewModel = ForumViewModel(
        observeThreads = ObserveThreadsUseCase(store),
        refreshThreads = RefreshThreadsUseCase(store),
    )

    private fun sampleThreads(): List<Thread> = listOf(
        Thread(thread_id = 1L, title = "Coffee machine broken", message_count = 3),
        Thread(thread_id = 2L, title = "Teletrabajo policy", message_count = 8),
    )

    @Test
    fun `init triggers a single refresh`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = FakeForumStore()
        newViewModel(store)
        advanceUntilIdle()

        assertEquals(1, store.refreshThreadsInvocations)
    }

    @Test
    fun `threads mirror the store feed for active subscribers`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeForumStore(initialThreads = sampleThreads())
            val vm = newViewModel(store)
            backgroundScope.launch { vm.threads.collect {} }
            advanceUntilIdle()

            assertEquals(2, vm.threads.value.size)
            assertEquals("Coffee machine broken", vm.threads.value[0].title)
        }

    @Test
    fun `threads keep stable identity across emissions`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val first = Thread(thread_id = 1L, title = "First", message_count = 0)
            val store = FakeForumStore(initialThreads = listOf(first))
            val vm = newViewModel(store)
            backgroundScope.launch { vm.threads.collect {} }
            advanceUntilIdle()

            val firstSnapshot = vm.threads.value
            store.emitThreads(listOf(first, Thread(thread_id = 2L, title = "Second")))
            advanceUntilIdle()

            // The previously emitted thread keeps the exact same instance reference so the
            // UI diffing layer can short-circuit.
            assertSame(firstSnapshot[0], vm.threads.value[0])
        }

    @Test
    fun `refresh forwards to the store again`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = FakeForumStore()
        val vm = newViewModel(store)
        advanceUntilIdle()
        assertEquals(1, store.refreshThreadsInvocations)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(2, store.refreshThreadsInvocations)
    }
}
