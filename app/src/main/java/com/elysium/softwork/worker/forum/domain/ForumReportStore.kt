package com.elysium.softwork.worker.forum.domain

import com.elysium.softwork.worker.forum.domain.model.ForumReport

/**
 * Domain contract for submitting and managing forum reports.
 */
interface ForumReportStore {
    /**
     * Submits a new report to the backend.
     *
     * @param report the report details to send.
     * @return [Result] containing the server-assigned report if successful.
     */
    suspend fun submit(report: ForumReport): Result<ForumReport>
}
