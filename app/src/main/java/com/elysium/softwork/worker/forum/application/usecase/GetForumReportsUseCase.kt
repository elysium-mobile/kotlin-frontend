package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.ForumReport

/**
 * Fetches the authenticated worker's submitted reports with their current status.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store report data port that performs the network call (with a bundled-sample
 *   fallback while the backend is unreachable).
 */
class GetForumReportsUseCase(private val store: ForumReportStore) {

    /** @return [Result.success] with the report list or [Result.failure] on error. */
    suspend operator fun invoke(): Result<List<ForumReport>> = store.list()
}
