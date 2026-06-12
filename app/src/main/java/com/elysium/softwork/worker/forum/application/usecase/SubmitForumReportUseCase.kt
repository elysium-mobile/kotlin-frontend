package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.ForumReport

/**
 * Submits a forum report against a post.
 *
 * Owns the assembly of the [ForumReport] entity from its raw parts, so the construction
 * rules (server-assigned fields left `null`, anonymity flag carried verbatim) live in one
 * place instead of inside a UI state holder.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store report data port that performs the network call.
 */
class SubmitForumReportUseCase(private val store: ForumReportStore) {

    /**
     * Builds and submits the report.
     *
     * @param postId identifier of the post being reported.
     * @param typeKey irregularity-type wire key.
     * @param areaKey area wire key.
     * @param description detailed explanation; trimmed before dispatch.
     * @param date approximate incident date as entered by the worker.
     * @param isAnonymous whether the report hides the reporter's identity.
     * @return [Result.success] with the server-acknowledged [ForumReport] or
     *   [Result.failure] on error.
     */
    suspend operator fun invoke(
        postId: String,
        typeKey: String,
        areaKey: String,
        description: String,
        date: String,
        isAnonymous: Boolean,
    ): Result<ForumReport> = store.submit(
        ForumReport(
            postId = postId,
            type = typeKey,
            area = areaKey,
            description = description.trim(),
            date = date,
            isAnonymous = isAnonymous,
        ),
    )
}
