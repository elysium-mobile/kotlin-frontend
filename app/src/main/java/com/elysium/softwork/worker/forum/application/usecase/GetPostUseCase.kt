package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.domain.model.Post

/**
 * One-shot lookup of a single post by its stable identifier.
 *
 * Resolves from the local cache; used by the thread-detail flow where the post is known
 * to be cached because the worker navigated from the feed.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port backed by the local cache.
 */
class GetPostUseCase(private val store: PostStore) {

    /**
     * @param postId stable post identifier from the navigation argument.
     * @return the cached [Post], or `null` when no row matches (e.g. cache was wiped).
     */
    suspend operator fun invoke(postId: String): Post? = store.getById(postId)
}
