package com.elysium.softwork.worker.forum.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Data model for a Forum Report (Denuncia).
 * Follows the bean/pragmatic shortcut: one class for both request and response.
 *
 * @property id identifier assigned by the server.
 * @property postId identifier of the post being reported.
 * @property type type of irregularity (e.g., "Acoso", "Discriminación").
 * @property area involved area or department.
 * @property description detailed explanation of the report.
 * @property date approximate date of the incident.
 * @property isAnonymous whether the report is sent anonymously.
 */
data class ForumReport(
    @SerializedName("id") val id: String? = null,
    @SerializedName("postId") val postId: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("isAnonymous") val isAnonymous: Boolean = true
)
