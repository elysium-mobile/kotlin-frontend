package com.elysium.softwork.payment.membership.domain.model

/**
 * A benefit included in a [MembershipPlan] — the annotation-free bean for the `benefits`
 * endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The owning plan arrives under `membershipPlanId` on the request
 * and `membership_plan_id` on the response, so both coexist as nullable fields. The plan's
 * `benefit_response_list` nests these, and the UI renders [title] as a feature bullet.
 *
 * @property benefit_id primary key returned by every benefit response.
 * @property title short benefit headline (rendered as a plan feature row).
 * @property description benefit description.
 * @property membershipPlanId owning plan on the **create request** (`membershipPlanId`).
 * @property membership_plan_id owning plan on the **response** (`membership_plan_id`).
 */
data class Benefit(
    val benefit_id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val membershipPlanId: Long? = null,
    val membership_plan_id: Long? = null,
)
