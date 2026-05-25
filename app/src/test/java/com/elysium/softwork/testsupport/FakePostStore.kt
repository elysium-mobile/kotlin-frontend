package com.elysium.softwork.testsupport

import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory test double for [PostStore].
 *
 * Backs [observe] with a [MutableStateFlow] so tests can drive emissions through
 * [emit]. Recording counters let assertions verify that the ViewModel forwarded
 * [refresh] / [publish] calls without spying on viewModelScope.
 *
 * The double is intentionally synchronous and deterministic — coroutines that depend
 * on it never spin off real I/O; every suspending member returns immediately so unit
 * tests run on virtual time only.
 */
open class FakePostStore(initialPosts: List<Post> = emptyList()) : PostStore {

    private val _observed: MutableStateFlow<List<Post>> = MutableStateFlow(initialPosts)

    /** Tally of [refresh] invocations. */
    var refreshInvocations: Int = 0
        private set

    /** Tally of [publish] invocations. */
    var publishInvocations: Int = 0
        private set

    /** Result returned by the next [refresh] call. Defaults to success. */
    var nextRefreshResult: Result<Unit> = Result.success(Unit)

    /** Captured argument tuple for the most recent [publish] call. */
    var lastPublishArgs: PublishArgs? = null
        private set

    override fun observe(): Flow<List<Post>> = _observed

    override suspend fun refresh(): Result<Unit> {
        refreshInvocations += 1
        return nextRefreshResult
    }

    override suspend fun getById(id: String): Post? = _observed.value.firstOrNull { it.id == id }

    override suspend fun publish(
        title: String,
        content: String,
        category: String,
        authorName: String,
        isAnonymous: Boolean,
    ): Result<Post> {
        publishInvocations += 1
        val args = PublishArgs(title, content, category, authorName, isAnonymous)
        lastPublishArgs = args
        val created = Post(
            id = "published-$publishInvocations",
            authorName = authorName,
            isAnonymous = isAnonymous,
            title = title,
            content = content,
            category = category,
            timestamp = publishInvocations.toLong(),
            repliesCount = 0,
        )
        _observed.value += created
        return Result.success(created)
    }

    /** Test-only emission helper. Replaces the cached list seen by [observe] collectors. */
    fun emit(posts: List<Post>) {
        _observed.value = posts
    }

    /** Captured arguments for one [publish] call. */
    data class PublishArgs(
        val title: String,
        val content: String,
        val category: String,
        val authorName: String,
        val isAnonymous: Boolean,
    )
}
