package com.elysium.softwork.payment.membership.domain.model

/**
 * A SoftWork membership tier the worker can subscribe to.
 *
 * Immutable domain entity, framework-agnostic by design: property names match the backend
 * wire keys exactly so the data layer's JSON serializer resolves them by reflection
 * without mapping annotations. All fields default to empty values so partially populated
 * payloads construct cleanly.
 *
 * @property key stable identifier persisted as the active-plan flag once the worker
 *   activates a membership.
 * @property name display name (e.g. "Basic", "Plan Pro"). Always presented in English in
 *   this bounded context regardless of the active app locale.
 * @property monthlyPrice price label exactly as it should appear in the UI, including the
 *   currency symbol (e.g. "S/. 99"). Treating it as a pre-formatted string lets the backend
 *   own locale and currency choices instead of leaking them into the UI layer.
 * @property features bullet list of features included in this tier.
 * @property isRecommended `true` when the UI should highlight this tier as the recommended
 *   path (teal accent). Drives the per-card color scheme on the selection screen.
 */
data class MembershipPlan(
    val key: String = "",
    val name: String = "",
    val monthlyPrice: String = "",
    val features: List<String> = emptyList(),
    val isRecommended: Boolean = false,
)
