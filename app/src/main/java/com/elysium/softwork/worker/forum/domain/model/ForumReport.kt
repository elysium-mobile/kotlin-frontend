package com.elysium.softwork.worker.forum.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for a Forum Report (Denuncia).
 * Follows the bean/pragmatic shortcut: one class for both request and response.
 *
 * @property id identifier assigned by the server.
 * @property postId identifier of the post being reported.
 * @property type irregularity-type wire key. One of
 *   `com.elysium.softwork.shared.utils.values.ReportType.key`.
 * @property area area-wire key. One of `com.elysium.softwork.shared.utils.values.ReportArea.key`.
 * @property description detailed explanation of the report.
 * @property date approximate date of the incident.
 * @property isAnonymous whether the report is sent anonymously.
 * @property status current lifecycle wire key. One of
 *   `com.elysium.softwork.shared.utils.values.ReportStatus.key`. Only populated on the
 *   response of the list endpoint — the request body leaves it `null`.
 * @property createdAt epoch millis when the server first accepted the report. Used by the
 *   status screen to render the submission date.
 */
data class ForumReport(
    @SerializedName("id") val id: String? = null,
    @SerializedName("postId") val postId: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("isAnonymous") val isAnonymous: Boolean = true,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: Long? = null,
)
