package com.elysium.softwork.worker.forum.data.store

import com.elysium.softwork.shared.data.network.BadRequestException
import com.elysium.softwork.shared.data.network.BadRequestResponse
import com.elysium.softwork.worker.forum.data.network.ForumWebService
import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.Report
import com.google.gson.Gson
import retrofit2.Response

/**
 * Concrete [ForumReportStore] backed by the live FlowWork Spring Boot API.
 *
 * No mock harness, no `SampleReports`, no artificial delay: both operations drive
 * [ForumWebService] directly and wrap their result in [Result], parsing a `400` into a
 * [BadRequestException] so the presentation layer can read the `field_errors`.
 *
 * @param webService Retrofit contract for the forum endpoints (reports included).
 * @param gson deserializer for the structured `400` validation payload.
 */
class ForumReportStoreImpl(
    private val webService: ForumWebService,
    private val gson: Gson,
) : ForumReportStore {

    override suspend fun submit(report: Report): Result<Report> =
        runCatching { unwrap(webService.createReport(report)) }

    override suspend fun list(): Result<List<Report>> =
        runCatching { unwrapList(webService.getReports()) }

    /** Unwraps a single-object [response]; a `400` becomes a [BadRequestException]. */
    private fun <T> unwrap(response: Response<T>): T {
        if (response.isSuccessful) {
            return response.body() ?: error("Empty response body")
        }
        throwTyped(response)
    }

    /** Unwraps a list [response], tolerating an empty body as an empty list. */
    private fun <T> unwrapList(response: Response<List<T>>): List<T> {
        if (response.isSuccessful) {
            return response.body().orEmpty()
        }
        throwTyped(response)
    }

    /**
     * Converts a non-2xx [response] into a typed failure: a `400` into a [BadRequestException]
     * carrying the parsed [BadRequestResponse], anything else into an [IllegalStateException].
     */
    private fun throwTyped(response: Response<*>): Nothing {
        val rawError: String? = runCatching { response.errorBody()?.string() }.getOrNull()
        if (response.code() == HTTP_BAD_REQUEST) {
            val parsed: BadRequestResponse = rawError
                ?.let { runCatching { gson.fromJson(it, BadRequestResponse::class.java) }.getOrNull() }
                ?: BadRequestResponse(message = rawError)
            throw BadRequestException(parsed)
        }
        error("HTTP ${response.code()} ${response.message().ifBlank { rawError ?: "request failed" }}")
    }

    private companion object {
        const val HTTP_BAD_REQUEST: Int = 400
    }
}
