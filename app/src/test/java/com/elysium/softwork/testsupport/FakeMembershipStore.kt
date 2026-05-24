package com.elysium.softwork.testsupport

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory test double for [MembershipStore].
 *
 * All persisted state is held in plain [MutableStateFlow]s so a test can either drive
 * the public mutators ([activateMembership], [cancelSubscription], [addPaymentMethod])
 * exactly like production callers do, or pre-seed state by reaching for the `_*`
 * properties through [seedMembership] / [seedPaymentMethods]. The seeding helpers exist
 * because tests for the methods screen and settings flow need a non-empty starting
 * state without going through the full payment graph.
 *
 * @param availablePlans plan catalogue served to callers. Defaults to a two-tier mock
 *   covering the recommended/non-recommended branches.
 */
open class FakeMembershipStore(
    private val availablePlans: List<MembershipPlan> = DEFAULT_PLANS,
) : MembershipStore {

    private val _hasMembership: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val hasMembership: StateFlow<Boolean> = _hasMembership.asStateFlow()

    private val _currentPlanKey: MutableStateFlow<String?> = MutableStateFlow(null)
    override val currentPlanKey: StateFlow<String?> = _currentPlanKey.asStateFlow()

    private val _paymentMethods: MutableStateFlow<List<PaymentMethod>> = MutableStateFlow(emptyList())
    override val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    /** Most recent argument passed to [activateMembership], or `null` if never invoked. */
    var lastActivatedPlanKey: String? = null
        private set

    /** Tally of [cancelSubscription] invocations. */
    var cancelInvocations: Int = 0
        private set

    /** Most recent [PaymentMethod] passed to [addPaymentMethod], or `null` if never invoked. */
    var lastAddedPaymentMethod: PaymentMethod? = null
        private set

    override fun availablePlans(): List<MembershipPlan> = availablePlans

    override fun findPlan(planKey: String): MembershipPlan? =
        availablePlans.firstOrNull { it.key == planKey }

    override suspend fun addPaymentMethod(method: PaymentMethod) {
        lastAddedPaymentMethod = method
        _paymentMethods.value += method
    }

    override suspend fun activateMembership(planKey: String) {
        lastActivatedPlanKey = planKey
        _currentPlanKey.value = planKey
        _hasMembership.value = true
    }

    override suspend fun cancelSubscription() {
        cancelInvocations += 1
        _currentPlanKey.value = null
        _hasMembership.value = false
    }

    /** Pre-seeds the membership flags without going through the public activation path. */
    fun seedMembership(active: Boolean, planKey: String?) {
        _hasMembership.value = active
        _currentPlanKey.value = planKey
    }

    /** Pre-seeds the saved-cards list without going through the public mutator. */
    fun seedPaymentMethods(methods: List<PaymentMethod>) {
        _paymentMethods.value = methods
    }

    companion object {
        /** Default catalogue covering both the recommended and non-recommended branches. */
        val DEFAULT_PLANS: List<MembershipPlan> = listOf(
            MembershipPlan(
                key = "basic",
                name = "Basic",
                monthlyPrice = "S/. 59",
                features = listOf("Basic check-in", "Surveys"),
                isRecommended = false,
            ),
            MembershipPlan(
                key = "pro",
                name = "Plan Pro",
                monthlyPrice = "S/. 99",
                features = listOf("Basic check-in", "Surveys", "Workplace forum"),
                isRecommended = true,
            ),
        )
    }
}
