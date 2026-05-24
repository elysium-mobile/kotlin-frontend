package com.elysium.softwork.feedback.data.store

import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * In-memory [FeedbackStore] used to drive the AI chat surface without a backend.
 *
 * Conversation messages live in a [MutableStateFlow] for the lifetime of the process;
 * they are not persisted across cold starts. Sending a message appends the worker's
 * outgoing entry immediately, suspends for [MOCK_AI_DELAY_MS] to simulate the round-trip,
 * then appends a templated AI reply chosen deterministically from [RESPONSE_TEMPLATES].
 *
 * The templates are intentionally generic so any worker prompt receives a plausible
 * acknowledgement. Replace the body of `send` with a real Retrofit call when the backend
 * is available — the [FeedbackStore] contract does not need to change.
 */
class FeedbackStoreImpl : FeedbackStore {

    private val _conversation: MutableStateFlow<List<ChatMessage>> =
        MutableStateFlow(emptyList())

    override val conversation: StateFlow<List<ChatMessage>> = _conversation.asStateFlow()

    override suspend fun send(content: String): Result<Unit> = runCatching {
        val trimmed: String = content.trim()
        if (trimmed.isEmpty()) return@runCatching

        val now: Long = System.currentTimeMillis()
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = trimmed,
            isFromUser = true,
            timestamp = now,
        )
        _conversation.value = _conversation.value + userMessage

        delay(MOCK_AI_DELAY_MS)

        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = generateAiReply(trimmed),
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
        )
        _conversation.value = _conversation.value + aiMessage
    }

    /**
     * Selects one of [RESPONSE_TEMPLATES] using a stable hash of the worker's prompt so
     * the mock can be replayed deterministically in screenshots and demos. The function
     * is pure — replace it with the processor-side response when a backend ships.
     */
    private fun generateAiReply(prompt: String): String {
        val index: Int = (prompt.hashCode().mod(RESPONSE_TEMPLATES.size))
            .let { raw -> if (raw < 0) raw + RESPONSE_TEMPLATES.size else raw }
        return RESPONSE_TEMPLATES[index]
    }

    companion object {
        /** Simulated AI round-trip duration, in milliseconds. */
        private const val MOCK_AI_DELAY_MS: Long = 1_200L

        /** Canned replies cycled through by [generateAiReply]. */
        private val RESPONSE_TEMPLATES: List<String> = listOf(
            "Thanks for sharing. Can you tell me a bit more about that?",
            "I hear you. How does that affect your day-to-day work?",
            "Got it. Is there a specific moment that triggered this?",
            "Understood. I'll make sure your feedback reaches the right team.",
            "That's useful context. Anything else you'd like to add?",
        )
    }
}
