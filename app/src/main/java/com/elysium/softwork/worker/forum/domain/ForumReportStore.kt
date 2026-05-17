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

    /**
     * Returns the authenticated user's submitted reports with their current status.
     *
     * Used by the home-screen "report status" entry point to render the tracking list.
     * The implementation may fall back to a bundled sample list when the backend is
     * unreachable so the screen stays usable during demos.
     */
    suspend fun list(): Result<List<ForumReport>>
}
