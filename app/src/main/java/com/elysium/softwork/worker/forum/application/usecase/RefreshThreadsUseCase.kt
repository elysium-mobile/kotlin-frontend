package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.ForumStore

/**
 * Pulls the latest threads from the server into the local cache.
 *
 * Command side of the offline-first contract: on success every [ObserveThreadsUseCase]
 * subscriber re-emits automatically. On failure the cache keeps its last snapshot.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port that owns the network call and the cache write.
 */
class RefreshThreadsUseCase(private val store: ForumStore) {

    /** @return [Result.success] when the cache was updated, [Result.failure] on transport error. */
    suspend operator fun invoke(): Result<Unit> = store.refreshThreads()
}
