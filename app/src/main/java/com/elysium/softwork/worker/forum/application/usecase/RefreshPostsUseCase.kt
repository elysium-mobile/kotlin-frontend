package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.PostStore

/**
 * Pulls the latest posts from the server into the local cache.
 *
 * Command side of the offline-first contract: on success the cache is upserted and every
 * [ObservePostsUseCase] subscriber re-emits automatically. On failure the cache keeps its
 * last snapshot, so the feed stays usable offline.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port that owns the network call and the cache write.
 */
class RefreshPostsUseCase(private val store: PostStore) {

    /** @return [Result.success] when the cache was updated, [Result.failure] on transport error. */
    suspend operator fun invoke(): Result<Unit> = store.refresh()
}
