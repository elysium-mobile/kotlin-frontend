package com.elysium.softwork.worker.forum.data.network

import com.elysium.softwork.worker.forum.domain.model.ForumReport
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit definition for the Forum Report endpoints.
 */
interface ForumReportWebService {
    /**
     * Submits a new report.
     *
     * @param report the report body per the bean shortcut.
     * @return [Response] containing the created [ForumReport].
     */
    @POST("forum/reports")
    suspend fun submitReport(@Body report: ForumReport): Response<ForumReport>
}
