package com.elysium.softwork.feedback.application.viewmodel

import com.elysium.softwork.testsupport.FakeFeedbackStore
import com.elysium.softwork.testsupport.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [AiChatViewModel].
 *
 * Drives the AI chat flow against a [FakeFeedbackStore] backed by virtual-time
 * suspension. The test class installs [MainDispatcherRule] with a
 * [StandardTestDispatcher] because the assertions need to observe the transient
 * `isSending = true` snapshot between the user message append and the AI reply append.
 * An `UnconfinedTestDispatcher` would resume past the assertion eagerly.
 *
 * The [runTest] blocks share the dispatcher's scheduler so virtual time advances
 * deterministically across both the ViewModel's `viewModelScope.launch` and the
 * store's `delay(...)` inside `send`.
 *
 * Behaviors under test:
 *  - Initial state: empty messages, `isSending = false`.
 *  - On `send(content)`, the user message lands in [AiChatViewModel.messages]
 *    instantly, [AiChatViewModel.isSending] flips to `true`, and after the mocked
 *    delay the AI reply appends with `isFromUser = false`.
 *  - Blank input is rejected — no message appended, no store call.
 *  - Re-entrance: a second `send` while one is in flight is dropped (no fan-out).
 *  - The `finally` block restores `isSending = false` so a future failure can't strand
 *    the UI in the loading state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private fun newViewModel(store: FakeFeedbackStore = FakeFeedbackStore()): AiChatViewModel =
        AiChatViewModel(store)

    @Test
    fun `initial state exposes an empty conversation and is not sending`() {
        val vm = newViewModel()
        assertEquals(emptyList<Any>(), vm.messages.value)
        assertFalse(vm.isSending.value)
    }

    @Test
    fun `send is a no-op for blank input`() = runTest(mainDispatcherRule.testDispatcher) {
        val store = FakeFeedbackStore()
        val vm = newViewModel(store)

        vm.send("   ")
        advanceUntilIdle()

        assertEquals(0, store.sendInvocations)
        assertEquals(emptyList<Any>(), vm.messages.value)
        assertFalse(vm.isSending.value)
    }

    @Test
    fun `send appends the user message instantly and toggles isSending`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeFeedbackStore(mockReplyDelayMillis = 1_200L)
            val vm = newViewModel(store)

            vm.send("Hello there")
            // Schedule the launched coroutine onto the dispatcher.
            runCurrent()

            // The store appends the user message synchronously before suspending.
            assertEquals(1, vm.messages.value.size)
            val first = vm.messages.value[0]
            assertTrue(first.isFromUser)
            assertEquals("Hello there", first.content)

            // Loading indicator is visible mid-round-trip.
            assertTrue(vm.isSending.value)

            // Halfway through the virtual delay nothing else has happened.
            advanceTimeBy(500L)
            runCurrent()
            assertEquals(1, vm.messages.value.size)
            assertTrue(vm.isSending.value)
        }

    @Test
    fun `send drains the AI reply after the mock delay and clears isSending`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeFeedbackStore(mockReplyDelayMillis = 1_000L).apply {
                programmedReply = "Got it, logged."
            }
            val vm = newViewModel(store)

            vm.send("Need help with my schedule")
            advanceUntilIdle()

            val log = vm.messages.value
            assertEquals(2, log.size)

            val (user, ai) = log[0] to log[1]
            assertTrue(user.isFromUser)
            assertEquals("Need help with my schedule", user.content)
            assertFalse(ai.isFromUser)
            assertEquals("Got it, logged.", ai.content)

            assertFalse(vm.isSending.value)
            assertEquals(1, store.sendInvocations)
            assertNotNull(store.lastSentContent)
        }

    @Test
    fun `send ignores a second invocation while one is already in flight`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeFeedbackStore(mockReplyDelayMillis = 1_000L)
            val vm = newViewModel(store)

            vm.send("first")
            runCurrent()
            assertTrue(vm.isSending.value)

            // Second attempt while the first one is still suspended.
            vm.send("second")
            runCurrent()

            // Drain the first send; the second one must NOT have queued.
            advanceUntilIdle()

            assertEquals(1, store.sendInvocations)
            // Conversation contains only one user message + one AI reply.
            assertEquals(2, vm.messages.value.size)
            assertEquals("first", vm.messages.value[0].content)
        }

    @Test
    fun `consecutive sends drain in order once each resolves`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = FakeFeedbackStore(mockReplyDelayMillis = 800L)
            val vm = newViewModel(store)

            vm.send("first")
            advanceUntilIdle()

            vm.send("second")
            advanceUntilIdle()

            val log = vm.messages.value
            assertEquals(4, log.size)
            assertEquals("first", log[0].content)
            assertEquals("second", log[2].content)
            assertEquals(2, store.sendInvocations)
            assertFalse(vm.isSending.value)
        }
}
