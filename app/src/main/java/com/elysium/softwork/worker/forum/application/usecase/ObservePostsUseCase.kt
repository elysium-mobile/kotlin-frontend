package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Streams the cached forum feed.
 *
 * Query side of the offline-first contract: the returned [Flow] re-emits whenever the
 * local cache changes (a refresh upserted rows, a publishing inserted one). It never
 * performs network I/O itself — pair with [RefreshPostsUseCase] for that.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port backed by the local cache.
 */
class ObservePostsUseCase(private val store: PostStore) {

    /** @return cold flow of the full cached feed, oldest data source of truth first. */
    operator fun invoke(): Flow<List<Post>> = store.observe()
}
