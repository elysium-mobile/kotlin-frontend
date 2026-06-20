package com.elysium.softwork.feedback.domain.model

/**
 * An HR survey the worker can answer — the single annotation-free bean for the `surveys`
 * endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Framework-agnostic by design: property names match the backend wire keys exactly, so the
 * JSON serializer resolves them by reflection without `@SerializedName`. The backend uses a
 * mixed snake_case/camelCase contract where request and response keys diverge for the same
 * concept, so those asymmetric keys coexist here as nullable fields and a given endpoint
 * fills only its subset:
 * - **target type**: request key `targetType`, response key `target_type`.
 * - **expiration**: request key `expirationType` (the DTO's quirky name), response key
 *   `expiration_time`.
 *
 * @property survey_id primary key returned by every survey response.
 * @property title survey headline rendered in the card.
 * @property description one-line context shown beneath the title.
 * @property targetType audience selector on the **create request** (`targetType`).
 * @property target_type audience selector on the **response** (`target_type`).
 * @property expirationType expiration on the **create request** (`expirationType`).
 * @property expiration_time expiration on the **response** (`expiration_time`).
 */
data class Survey(
    val survey_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val targetType: String? = null,
    val target_type: String? = null,
    val expirationType: String? = null,
    val expiration_time: String? = null,
)
