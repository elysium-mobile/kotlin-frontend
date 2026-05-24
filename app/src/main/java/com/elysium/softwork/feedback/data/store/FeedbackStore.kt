package com.elysium.softwork.feedback.data.store

import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

/**
 * AI chat data port.
 *
 * Owns the conversation log between the worker and the FlowWork AI assistant. The
 * conversation is exposed as a [StateFlow] so consumers (the chat ViewModel) can read the
 * current snapshot synchronously on first composition and continue to react to future
 * mutations without re-querying.
 *
 * The contract is intentionally backend-agnostic: callers depend only on these two
 * methods, never on the concrete storage backend.
 */
interface FeedbackStore {

    /**
     * Reactive view of the conversation log. The value is ordered chronologically, with
     * the oldest message first. Implementations guarantee that the returned reference
     * survives recompositions and only emits when the log actually changes.
     */
    val conversation: StateFlow<List<ChatMessage>>

    /**
     * Records the worker's outgoing message and produces a matching AI reply.
     *
     * Suspends for the duration of the simulated round-trip (≈ 1.2 seconds in the mocked
     * implementation). On return, the [conversation] flow has emitted twice: once with
     * the worker's message appended, and once more with the AI reply appended. Blank
     * content is ignored — callers do not need to pre-validate.
     *
     * @param content text typed by the worker. Trimmed by the implementation.
     * @return [Result.success] with [Unit] when both messages were appended successfully,
     *   or [Result.failure] when the underlying call raised an exception. The mock never
     *   fails; the contract leaves room for real network errors once a backend is wired.
     */
    suspend fun send(content: String): Result<Unit>
}
