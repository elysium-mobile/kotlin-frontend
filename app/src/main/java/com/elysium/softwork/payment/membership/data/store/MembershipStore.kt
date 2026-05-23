package com.elysium.softwork.payment.membership.data.store

import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * Payment & membership data port.
 *
 * Owns the worker's membership lifecycle (activation, cancellation, current tier) and the
 * catalogue of saved cards. Exposes [hasMembership] as a [StateFlow] so the host Activity
 * can reactively swap between the main app shell and the payment graph the moment the
 * flag flips — that is the mechanism that powers the reactive "cancel subscription"
 * routing.
 *
 * The contract is intentionally backend-agnostic: callers depend on these methods and the
 * three [StateFlow]s, never on the concrete storage backend.
 */
interface MembershipStore {

    /**
     * Reactive flag describing whether the worker currently has an active membership.
     * Backed by `KEY_HAS_MEMBERSHIP` in `SharedPrefsManager`. Collected by `MainActivity`
     * to decide whether to mount the main shell or the payment graph.
     */
    val hasMembership: StateFlow<Boolean>

    /**
     * Stable identifier of the active plan, or `null` when no membership is active. Backed
     * by `KEY_CURRENT_PLAN`. Updated atomically with [hasMembership] by [activateMembership]
     * and [cancelSubscription].
     */
    val currentPlanKey: StateFlow<String?>

    /** Reactive list of saved payment methods. */
    val paymentMethods: StateFlow<List<PaymentMethod>>

    /**
     * Snapshot of the available plan catalogue. Returns the full list because callers
     * (the selection screen, the methods recap) need it eagerly to render their layouts.
     */
    fun availablePlans(): List<MembershipPlan>

    /**
     * Resolves a [MembershipPlan] by its [planKey], or `null` when no such plan exists.
     * Used by `PaymentMethodsScreen` to recap the price after the user selected a plan
     * on the previous screen.
     */
    fun findPlan(planKey: String): MembershipPlan?

    /**
     * Persists [method] into the saved-cards list. Suspending so implementations may
     * round-trip the value through a remote tokenization step before exposing it on
     * [paymentMethods].
     */
    suspend fun addPaymentMethod(method: PaymentMethod)

    /**
     * Flips [hasMembership] to `true`, stores [planKey] as the active tier, and persists
     * both to `SharedPrefsManager`. Triggered by the user tapping "Main menu" on
     * `PaymentSuccessScreen`.
     *
     * @param planKey stable [MembershipPlan.key] of the plan the worker just paid for.
     */
    suspend fun activateMembership(planKey: String)

    /**
     * Atomically clears the membership flags. Wipes `KEY_HAS_MEMBERSHIP` /
     * `KEY_CURRENT_PLAN`, emits the new values, and lets the Activity-level collector swap
     * the user out of the main shell and back into the membership selection screen.
     *
     * Card data is intentionally left intact so the worker can re-subscribe without
     * re-entering their card details.
     */
    suspend fun cancelSubscription()
}
