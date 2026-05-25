package com.elysium.softwork.testsupport

import com.elysium.softwork.feedback.data.store.FeedbackStore
import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory test double for [FeedbackStore].
 *
 * Models the production contract: the worker's outgoing message is appended immediately,
 * the call suspends for [mockReplyDelayMillis] virtual milliseconds, then a templated AI
 * reply is appended. The suspension uses [delay] so callers driving the test through
 * `runTest` advance virtual time deterministically without ever blocking a real thread.
 *
 * Use the `programReply(...)` mutator to set the next AI reply content. Defaults to a
 * fixed string so tests that do not care can ignore it.
 *
 * @param mockReplyDelayMillis virtual delay before the AI reply is appended. Defaults to
 *   1.2 seconds, mirroring the production mock.
 */
open class FakeFeedbackStore(
    private val mockReplyDelayMillis: Long = DEFAULT_REPLY_DELAY_MS,
) : FeedbackStore {

    private val _conversation: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    override val conversation: StateFlow<List<ChatMessage>> = _conversation.asStateFlow()

    /** Reply content the next [send] call will append. */
    var programmedReply: String = DEFAULT_REPLY

    /** Tally of [send] invocations. */
    var sendInvocations: Int = 0
        private set

    /** Argument of the most recent [send] call. */
    var lastSentContent: String? = null
        private set

    override suspend fun send(content: String): Result<Unit> {
        val trimmed: String = content.trim()
        if (trimmed.isBlank()) return Result.success(Unit)
        sendInvocations += 1
        lastSentContent = trimmed

        // Append the worker's message immediately, mirroring the production contract.
        _conversation.value += ChatMessage(
            id = "user-$sendInvocations",
            content = trimmed,
            isFromUser = true,
            timestamp = sendInvocations.toLong(),
        )

        // Simulate the round-trip using virtual time so runTest advances through it.
        delay(mockReplyDelayMillis)

        _conversation.value += ChatMessage(
            id = "ai-$sendInvocations",
            content = programmedReply,
            isFromUser = false,
            timestamp = sendInvocations.toLong() + 1L,
        )
        return Result.success(Unit)
    }

    /** Seeds the conversation log without going through [send]. */
    fun seedConversation(messages: List<ChatMessage>) {
        _conversation.value = messages
    }

    companion object {
        const val DEFAULT_REPLY_DELAY_MS: Long = 1_200L
        const val DEFAULT_REPLY: String = "Thanks for sharing!"
    }
}
