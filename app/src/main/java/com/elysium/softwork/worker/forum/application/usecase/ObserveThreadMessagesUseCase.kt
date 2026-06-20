package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.ForumStore
import com.elysium.softwork.worker.forum.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Streams the cached messages of a single thread (the thread-detail reply list).
 *
 * Query side of the offline-first contract: pair with [RefreshThreadMessagesUseCase] to pull
 * fresh data from the network. Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port backed by the local cache.
 */
class ObserveThreadMessagesUseCase(private val store: ForumStore) {

    /**
     * @param threadId thread whose messages to observe.
     * @return cold flow of the cached messages for [threadId], oldest first.
     */
    operator fun invoke(threadId: Long): Flow<List<Message>> = store.observeMessages(threadId)
}
