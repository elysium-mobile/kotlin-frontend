package com.elysium.softwork.worker.forum.domain

import com.elysium.softwork.worker.forum.domain.model.Report

/**
 * Domain contract for submitting and listing content/conduct reports.
 *
 * Returns [Result] so callers get a single error channel — a `400 Bad Request` surfaces as a
 * [com.elysium.softwork.shared.data.network.BadRequestException].
 */
interface ForumReportStore {

    /** Submits a new report (`POST /api/v1/reports`). */
    suspend fun submit(report: Report): Result<Report>

    /** Lists the submitted reports (`GET /api/v1/reports`). */
    suspend fun list(): Result<List<Report>>
}
