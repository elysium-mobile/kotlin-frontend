package com.elysium.softwork.worker.forum.data.store

import com.elysium.softwork.worker.forum.data.network.ForumReportWebService
import com.elysium.softwork.worker.forum.domain.ForumReportStore
import com.elysium.softwork.worker.forum.domain.model.ForumReport
import com.elysium.softwork.shared.utils.values.ReportStatus
import kotlinx.coroutines.delay

/**
 * Implementation of [ForumReportStore] using Retrofit.
 *
 * Phase 4 demo behaviour: [list] falls back to a bundled sample list when the network
 * round-trip fails so the reports-status screen has something to render without a backend.
 * Remove [SampleReports] when the `/forum/reports` GET endpoint is live.
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

    override suspend fun list(): Result<List<ForumReport>> = runCatching {
        try {
            val response = webService.listReports()
            if (response.isSuccessful) {
                return@runCatching response.body() ?: emptyList()
            }
        } catch (_: Throwable) {
            // Fall through to the seed branch below.
        }
        // Network unreachable or non-2xx: surface the bundled samples so the UI keeps working
        // during the no-backend demo. Replace with `throw` once the endpoint is live.
        SampleReports
    }

    private companion object {
        /**
         * Bundled sample reports used by [list] when the network call fails. Each entry
         * exercises a different [ReportStatus] so the status pills can be reviewed without
         * actually submitting anything.
         */
        private val NOW: Long = System.currentTimeMillis()

        val SampleReports: List<ForumReport> = listOf(
            ForumReport(
                id = "sample-1",
                postId = "seed-2",
                type = "Acoso",
                area = "Recursos Humanos",
                description = "Reporte de comentarios inapropiados en una reunión interna.",
                date = "12 / 05 / 2026",
                isAnonymous = true,
                status = ReportStatus.UNDER_REVIEW.key,
                createdAt = NOW - 1_000L * 60 * 60 * 24 * 2,
            ),
            ForumReport(
                id = "sample-2",
                postId = "seed-1",
                type = "Ética",
                area = "Operaciones",
                description = "Posible conflicto de intereses en la asignación de proyectos.",
                date = "03 / 05 / 2026",
                isAnonymous = true,
                status = ReportStatus.PENDING.key,
                createdAt = NOW - 1_000L * 60 * 60 * 24 * 7,
            ),
            ForumReport(
                id = "sample-3",
                postId = "seed-3",
                type = "Seguridad",
                area = "Tecnología",
                description = "Acceso indebido a un repositorio interno.",
                date = "22 / 04 / 2026",
                isAnonymous = false,
                status = ReportStatus.RESOLVED.key,
                createdAt = NOW - 1_000L * 60 * 60 * 24 * 25,
            ),
        )
    }
}
