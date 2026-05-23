package com.elysium.softwork.feedback.domain.model

import com.google.gson.annotations.SerializedName

/**
 * A pending HR survey the worker is invited to answer.
 *
 * The same instance flows through the Retrofit web service request/response and into the
 * in-memory catalogue. All fields default to nullable-friendly empty values so partial
 * server responses do not crash deserialization.
 *
 * @property id stable identifier issued by the backend (or a mock literal in the seed set).
 * @property title short headline rendered in the survey card.
 * @property description one-line context shown beneath the title.
 */
data class Survey(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
)
