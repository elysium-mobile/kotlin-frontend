package com.elysium.softwork.worker.forum.data.store

import com.elysium.softwork.worker.forum.data.network.ForumReportWebService
import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.ForumReport
import kotlinx.coroutines.delay

/**
 * Implementation of [ForumReportStore] using Retrofit.
 *
 * @property webService the Retrofit service for reports.
 */
class ForumReportStoreImpl(
    private val webService: ForumReportWebService,
) : ForumReportStore {

    override suspend fun submit(report: ForumReport): Result<ForumReport> = runCatching {
        // For development/demo purposes, we add a artificial delay.
        // In a real scenario, this would only be the network call.
        delay(1000)
        
        val response = webService.submitReport(report)
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Failed to submit report: ${response.code()}")
        }
    }
}
