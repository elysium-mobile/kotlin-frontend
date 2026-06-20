package com.elysium.softwork.payment.membership.domain.model

/**
 * A subscription plan tier — the annotation-free bean for the `membership-plans` endpoints
 * (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The plan name and owning membership arrive under camelCase keys
 * on the request and snake_case on the response, so both spellings coexist as nullable
 * fields. The former `monthlyPrice` string is replaced by the backend's integer [price]; the
 * UI formats it for display. The former hardcoded `features` list is now the nested
 * [benefit_response_list].
 *
 * @property plan_id primary key returned by every plan response; the stable identifier the
 *   membership gate persists as the active plan.
 * @property planName plan name on the **create request** (`planName`).
 * @property plan_name plan name on the **response** (`plan_name`).
 * @property price plan price (the UI formats the currency for display).
 * @property membershipId owning membership on the **create request** (`membershipId`).
 * @property membership_id owning membership on the **response** (`membership_id`); forwarded
 *   when creating the purchase order.
 * @property benefit_response_list nested benefits; their titles render as plan feature rows.
 */
data class MembershipPlan(
    val plan_id: Long? = null,
    val planName: String? = null,
    val plan_name: String? = null,
    val price: Int? = null,
    val membershipId: Long? = null,
    val membership_id: Long? = null,
    val benefit_response_list: List<Benefit>? = null,
)
