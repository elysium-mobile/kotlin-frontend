package com.elysium.softwork.worker.forum.application.usecase

import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.Report

/**
 * Fetches the submitted reports for the report-status screen.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store report data port that performs the network call.
 */
class GetForumReportsUseCase(private val store: ForumReportStore) {

    /** @return [Result.success] with the report list or [Result.failure] on error. */
    suspend operator fun invoke(): Result<List<Report>> = store.list()
}
