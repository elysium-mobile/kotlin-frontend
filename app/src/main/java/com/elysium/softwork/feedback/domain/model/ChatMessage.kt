package com.elysium.softwork.feedback.domain.model

/**
 * One entry in the AI chat conversation log.
 *
 * Immutable domain entity, framework-agnostic by design: property names match the backend
 * wire keys exactly so the data layer's JSON serializer resolves them by reflection
 * without mapping annotations. Every field carries a non-null default so a partial
 * payload constructs cleanly.
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
    val id: String = "",
    val content: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Long = 0L,
)
