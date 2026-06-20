package com.elysium.softwork.payment.membership.domain.model

/**
 * A membership lifecycle record — the annotation-free bean for the `memberships` endpoints
 * (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The start/over/status fields arrive under camelCase keys on the
 * request and snake_case on the response, so both spellings coexist as nullable fields.
 *
 * @property membership_id primary key returned by every membership response.
 * @property membershipStart start date on the **create request** (`membershipStart`).
 * @property membership_start start date on the **response** (`membership_start`).
 * @property membershipOver end date on the **create request** (`membershipOver`).
 * @property membership_over end date on the **response** (`membership_over`).
 * @property membershipStatus status on the **create request** (`membershipStatus`:
 *   `ACTIVE` / `PENDING` / `INACTIVE`).
 * @property membership_status status on the **response** (`membership_status`).
 */
data class Membership(
    val membership_id: Long? = null,
    val membershipStart: String? = null,
    val membership_start: String? = null,
    val membershipOver: String? = null,
    val membership_over: String? = null,
    val membershipStatus: String? = null,
    val membership_status: String? = null,
)
