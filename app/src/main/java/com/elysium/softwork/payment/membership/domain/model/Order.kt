package com.elysium.softwork.payment.membership.domain.model

/**
 * A purchase order for a membership — the annotation-free bean for the `orders` endpoints
 * (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The account and membership ids arrive under camelCase keys on the
 * request and snake_case on the response, so both spellings coexist as nullable fields.
 *
 * @property order_id primary key returned by every order response.
 * @property userAccountId buyer account on the **create request** (`userAccountId`).
 * @property user_account_id buyer account on the **response** (`user_account_id`).
 * @property amount order amount.
 * @property membershipId purchased membership on the **create request** (`membershipId`).
 * @property membership_id purchased membership on the **response** (`membership_id`).
 */
data class Order(
    val order_id: Long? = null,
    val userAccountId: Long? = null,
    val user_account_id: Long? = null,
    val amount: Int? = null,
    val membershipId: Long? = null,
    val membership_id: Long? = null,
)
