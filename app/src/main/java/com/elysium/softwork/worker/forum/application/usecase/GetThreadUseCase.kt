package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.ForumStore
import com.elysium.softwork.worker.forum.domain.model.Thread

/**
 * One-shot lookup of a single thread by its id.
 *
 * Resolves from the local cache; used by the thread-detail flow where the thread is known to
 * be cached because the worker navigated from the feed.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port backed by the local cache.
 */
class GetThreadUseCase(private val store: ForumStore) {

    /**
     * @param threadId stable thread identifier from the navigation argument.
     * @return the cached [Thread], or `null` when no row matches.
     */
    suspend operator fun invoke(threadId: Long): Thread? = store.getThread(threadId)
}
