package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.domain.model.Post

/**
 * Publishes a new forum post.
 *
 * Owns two business rules so every caller behaves identically:
 *  - title and content are trimmed before they reach the data port;
 *  - when the post is anonymous, the author name is blanked so the identity can never
 *    leak through the payload regardless of what the caller passes.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store forum data port that attempts the network publish and falls back to a
 *   local insert so the worker sees their post immediately.
 */
class PublishPostUseCase(private val store: PostStore) {

    /**
     * Executes the publishing.
     *
     * @param title post title; trimmed before dispatch.
     * @param content post body; trimmed before dispatch.
     * @param categoryKey stable wire key of the selected forum category.
     * @param authorName the worker's display name; blanked when [isAnonymous] is `true`.
     * @param isAnonymous whether the post hides the author identity.
     * @return [Result.success] with the created [Post] or [Result.failure] on error.
     */
    suspend operator fun invoke(
        title: String,
        content: String,
        categoryKey: String,
        authorName: String,
        isAnonymous: Boolean,
    ): Result<Post> = store.publish(
        title = title.trim(),
        content = content.trim(),
        category = categoryKey,
        authorName = if (isAnonymous) "" else authorName,
        isAnonymous = isAnonymous,
    )
}
