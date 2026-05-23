package com.elysium.softwork.payment.membership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * A SoftWork membership tier the worker can subscribe to.
 *
 * The same instance flows through the Retrofit web service request/response and into the
 * in-memory catalogue exposed by the store. All fields default to nullable-friendly empty
 * values so partial server responses do not crash deserialization.
 *
 * @property key stable identifier used as the value of `KEY_CURRENT_PLAN` in
 *   `SharedPrefsManager` once the worker activates a membership.
 * @property name display name (e.g. "Basic", "Plan Pro"). Always presented in English in
 *   this bounded context regardless of the active app locale.
 * @property monthlyPrice price label exactly as it should appear in the UI, including the
 *   currency symbol (e.g. "S/. 99"). Treating it as a pre-formatted string lets the backend
 *   own locale and currency choices instead of leaking them into Compose.
 * @property features bullet list of features included in this tier.
 * @property isRecommended `true` when the UI should highlight this tier as the recommended
 *   path (teal accent). Drives the per-card color scheme on `MembershipSelectionScreen`.
 */
data class MembershipPlan(
    @SerializedName("key") val key: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("monthlyPrice") val monthlyPrice: String = "",
    @SerializedName("features") val features: List<String> = emptyList(),
    @SerializedName("isRecommended") val isRecommended: Boolean = false,
)
