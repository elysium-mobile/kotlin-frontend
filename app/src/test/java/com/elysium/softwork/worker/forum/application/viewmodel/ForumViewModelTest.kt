package com.elysium.softwork.worker.forum.application.viewmodel

import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.testsupport.FakePostStore
import com.elysium.softwork.testsupport.MainDispatcherRule
import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ForumViewModel].
 *
 * The ViewModel exposes [ForumViewModel.posts] as a `stateIn(WhileSubscribed)` flow, so
 * the upstream [com.elysium.softwork.worker.forum.data.store.PostStore.observe] only
 * collects while there is an active subscriber. Tests therefore launch a no-op
 * collector inside `runTest`'s `backgroundScope` so the flow upstream activates and
 * `posts.value` mirrors the latest emission from the [FakePostStore].
 *
 * The dispatcher rule installs an `UnconfinedTestDispatcher` so the
 * `viewModelScope.launch { store.refresh() }` block in `init` runs eagerly. The
 * `refresh` and `selectCategory` paths are exercised independently to keep failure
 * diagnostics tight.
 *
 * Behaviors under test:
 *  - `init` calls `refresh()` exactly once.
 *  - `posts` re-emits when the store backing flow emits a fresh list.
 *  - `selectCategory(...)` filters the feed by the wire key.
 *  - `selectCategory(null)` re-exposes the full feed.
 *  - List items keep stable identity across emissions (same `id` → same post instance).
 *  - `refresh()` forwards to the store an additional time.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private fun samplePosts(): List<Post> = listOf(
        Post(
            id = "p-1",
            authorName = "Alice",
            title = "Coffee machine broken",
            content = "Help",
            category = ForumCategory.SUGGESTIONS.key,
            timestamp = 1L,
        ),
        Post(
            id = "p-2",
            authorName = "Bob",
            title = "Is anyone coming to the offsite?",
            content = "RSVP",
            category = ForumCategory.QUESTIONS.key,
            timestamp = 2L,
        ),
        Post(
            id = "p-3",
            authorName = "Cara",
            title = "Holiday party",
            content = "Friday",
            category = ForumCategory.EVENTS.key,
            timestamp = 3L,
        ),
    )

    @Test
    fun `init triggers a single refresh on the store`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = FakePostStore()
        ForumViewModel(store)
        advanceUntilIdle()

        assertEquals(1, store.refreshInvocations)
    }

    @Test
    fun `posts emits the store snapshot when subscribed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakePostStore()
            val vm = ForumViewModel(store)
            backgroundScope.launch { vm.posts.collect {} }

            store.emit(samplePosts())
            advanceUntilIdle()

            assertEquals(3, vm.posts.value.size)
            assertEquals(listOf("p-1", "p-2", "p-3"), vm.posts.value.map { it.id })
        }

    @Test
    fun `selectCategory filters the feed by the wire key`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakePostStore(initialPosts = samplePosts())
            val vm = ForumViewModel(store)
            backgroundScope.launch { vm.posts.collect {} }
            advanceUntilIdle()

            vm.selectCategory(ForumCategory.QUESTIONS)
            advanceUntilIdle()

            val filtered = vm.posts.value
            assertEquals(1, filtered.size)
            assertEquals("p-2", filtered[0].id)
            assertEquals(ForumCategory.QUESTIONS, vm.selectedCategory.value)
        }

    @Test
    fun `selectCategory null re-exposes the full feed`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakePostStore(initialPosts = samplePosts())
            val vm = ForumViewModel(store)
            backgroundScope.launch { vm.posts.collect {} }

            vm.selectCategory(ForumCategory.EVENTS)
            advanceUntilIdle()
            assertEquals(1, vm.posts.value.size)

            vm.selectCategory(null)
            advanceUntilIdle()
            assertEquals(3, vm.posts.value.size)
        }

    @Test
    fun `refresh forwards to the store on demand`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakePostStore()
            val vm = ForumViewModel(store)
            advanceUntilIdle()
            // init has already called refresh once.
            assertEquals(1, store.refreshInvocations)

            vm.refresh()
            advanceUntilIdle()

            assertEquals(2, store.refreshInvocations)
        }

    @Test
    fun `posts preserves stable item identity across emissions`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakePostStore()
            val vm = ForumViewModel(store)
            backgroundScope.launch { vm.posts.collect {} }

            val first = Post(id = "p-1", title = "First", category = ForumCategory.SUGGESTIONS.key)
            val second = Post(id = "p-2", title = "Second", category = ForumCategory.SUGGESTIONS.key)
            store.emit(listOf(first))
            advanceUntilIdle()
            val firstSnapshot = vm.posts.value
            assertEquals(1, firstSnapshot.size)

            // Add a second post; the original entry must keep its identity (`id`).
            store.emit(listOf(first, second))
            advanceUntilIdle()
            val secondSnapshot = vm.posts.value
            assertEquals(2, secondSnapshot.size)
            assertTrue(secondSnapshot.first().id == firstSnapshot.first().id)
            assertEquals(first, secondSnapshot.first())
        }
}
