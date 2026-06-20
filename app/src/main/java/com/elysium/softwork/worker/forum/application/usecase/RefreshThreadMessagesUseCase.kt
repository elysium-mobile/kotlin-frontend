package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.ForumStore

/**
 * Pulls the messages of a single thread from the server into the local cache.
 *
 * Command side of the offline-first contract for the thread-detail screen. Stateless; safe to
 * share a single instance process-wide.
 *
 * @param store forum data port that owns the network call and the cache write.
 */
class RefreshThreadMessagesUseCase(private val store: ForumStore) {

    /**
     * @param threadId thread whose messages to refresh.
     * @return [Result.success] when the cache was updated, [Result.failure] on transport error.
     */
    suspend operator fun invoke(threadId: Long): Result<Unit> = store.refreshMessages(threadId)
}
