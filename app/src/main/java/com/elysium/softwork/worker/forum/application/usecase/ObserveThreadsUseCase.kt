package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.ForumStore
import com.elysium.softwork.worker.forum.domain.model.Thread
import kotlinx.coroutines.flow.Flow

/**
 * Streams the cached forum thread feed.
 *
 * Query side of the offline-first contract: the returned [Flow] re-emits whenever the local
 * cache changes (a refresh upserted rows, a creation inserted one). It never performs network
 * I/O itself — pair with [RefreshThreadsUseCase].
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port backed by the local cache.
 */
class ObserveThreadsUseCase(private val store: ForumStore) {

    /** @return cold flow of the cached thread feed. */
    operator fun invoke(): Flow<List<Thread>> = store.observeThreads()
}
