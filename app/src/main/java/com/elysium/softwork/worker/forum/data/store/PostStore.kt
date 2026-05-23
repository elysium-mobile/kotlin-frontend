package com.elysium.softwork.worker.forum.data.store

import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Forum data port. Follows an **offline-first** pattern: the UI always observes the
 * cached [Flow] from Room ([observe]) and treats network calls ([refresh], [publish]) as
 * mutations that update the cache. This keeps the feed responsive even when the network is
 * slow or absent.
 */
interface PostStore {

    /** Live feed straight from the local cache. Never throws. */
    fun observe(): Flow<List<Post>>

    /**
     * Pulls the latest posts from the server and upserts them into the local cache.
     * Returns [Result.failure] when the network call fails — the [observe] stream stays on
     * the last cached snapshot and the UI keeps working.
     */
    suspend fun refresh(): Result<Unit>

    /** One-shot lookup used by the thread-detail screen. */
    suspend fun getById(id: String): Post?

    /**
     * Publishes a new post. On success the returned [Post] is inserted into the local cache,
     * which causes [observe] to re-emit. The optional [authorName] / [isAnonymous] toggle is
     * resolved by the caller (typically from `forum_anonymity` in
     * [com.elysium.softwork.shared.data.local.SharedPrefsManager]).
     */
    suspend fun publish(
        title: String,
        content: String,
        category: String,
        authorName: String,
        isAnonymous: Boolean,
    ): Result<Post>
}
