package com.elysium.softwork.worker.forum.domain.model

/**
 * A forum report raised by a worker against a post or workplace situation.
 *
 * Immutable domain entity, framework-agnostic by design: property names match the backend
 * wire keys exactly so the data layer's JSON serializer resolves them by reflection
 * without mapping annotations. All fields are nullable because the request and response
 * shapes fill different subsets (the request leaves [id], [status], and [createdAt] for
 * the server to assign).
 *
 * @property id identifier assigned by the server.
 * @property postId identifier of the post being reported.
 * @property type irregularity-type wire key. One of
 *   `com.elysium.softwork.shared.utils.values.ReportType.key`.
 * @property area area wire key. One of `com.elysium.softwork.shared.utils.values.ReportArea.key`.
 * @property description detailed explanation of the report.
 * @property date approximate date of the incident.
 * @property isAnonymous whether the report is sent anonymously.
 * @property status current lifecycle wire key. One of
 *   `com.elysium.softwork.shared.utils.values.ReportStatus.key`. Only populated on the
 *   response of the list operation — the request leaves it `null`.
 * @property createdAt epoch millis when the server first accepted the report. Used by the
 *   status screen to render the submission date.
 */
data class ForumReport(
    val id: String? = null,
    val postId: String? = null,
    val type: String? = null,
    val area: String? = null,
    val description: String? = null,
    val date: String? = null,
    val isAnonymous: Boolean = true,
    val status: String? = null,
    val createdAt: Long? = null,
)
