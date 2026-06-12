package com.elysium.softwork.feedback.application.usecase

import com.elysium.softwork.feedback.data.store.FeedbackStore

/**
 * Sends the worker's chat message to the AI assistant.
 *
 * Suspends for the duration of the round-trip; on return the conversation stream exposed
 * by [ObserveConversationUseCase] has emitted both the worker's message and the reply.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store feedback data port that records the message and produces the reply.
 */
class SendChatMessageUseCase(private val store: FeedbackStore) {

    /**
     * @param content text typed by the worker; the data port trims it and ignores blanks.
     * @return [Result.success] when both messages were appended, [Result.failure] on error.
     */
    suspend operator fun invoke(content: String): Result<Unit> = store.send(content)
}
