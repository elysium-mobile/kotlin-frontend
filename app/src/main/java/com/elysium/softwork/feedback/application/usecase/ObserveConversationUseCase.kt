package com.elysium.softwork.feedback.application.usecase

import com.elysium.softwork.feedback.data.store.FeedbackStore
import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the AI chat conversation log as a hot stream.
 *
 * Returns the store's [StateFlow] directly so consumers can read the current snapshot
 * synchronously on first composition — no default-state flash — and keep reacting to
 * future mutations.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store feedback data port owning the conversation log.
 */
class ObserveConversationUseCase(private val store: FeedbackStore) {

    /** @return hot stream of the chronologically ordered conversation log. */
    operator fun invoke(): StateFlow<List<ChatMessage>> = store.conversation
}
