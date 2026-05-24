package com.elysium.softwork.feedback.domain.model

import com.google.gson.annotations.SerializedName

/**
 * One entry in the AI chat conversation log.
 *
 * The same data class flows through any future Retrofit web service request/response and
 * into the in-memory conversation buffer exposed by the store. Every field carries a
 * non-null default so a partial server payload deserializes cleanly.
 *
 * @property id stable identifier — server-issued in production, locally generated via
 *   `UUID.randomUUID()` while the conversation is mocked.
 * @property content the textual body of the message, already localized by the producer.
 * @property isFromUser `true` when the message was authored by the worker. `false` when
 *   the message originated from the AI service. Drives bubble alignment and color on the
 *   chat screen.
 * @property timestamp wall-clock millisecond timestamp of when the message was created.
 *   Used for stable ordering and (eventually) for the bubble timestamp display.
 */
data class ChatMessage(
    @SerializedName("id") val id: String = "",
    @SerializedName("content") val content: String = "",
    @SerializedName("isFromUser") val isFromUser: Boolean = false,
    @SerializedName("timestamp") val timestamp: Long = 0L,
)
