package com.elysium.softwork.feedback.domain.model

/**
 * A pending HR survey the worker is invited to answer.
 *
 * Immutable domain entity, framework-agnostic by design: property names match the backend
 * wire keys exactly so the data layer's JSON serializer resolves them by reflection
 * without mapping annotations. All fields default to empty values so partial payloads
 * construct cleanly.
 *
 * @property id stable identifier issued by the backend (or a mock literal in the seed set).
 * @property title short headline rendered in the survey card.
 * @property description one-line context shown beneath the title.
 */
data class Survey(
    val id: String = "",
    val title: String = "",
    val description: String = "",
)
