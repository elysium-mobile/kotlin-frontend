package com.elysium.softwork.payment.membership.data.store

import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [MembershipStore] backed by [SharedPrefsManager] for the membership flags.
 *
 * The saved-cards list lives in a [MutableStateFlow] (lost across process death). The plan
 * catalogue is a hardcoded constant — swap [availablePlans] to a Retrofit-backed call
 * without changing any caller. Plan labels are presented in English regardless of the
 * active app locale.
 *
 * @param prefs persistent key-value store backing the membership flags.
 */
class MembershipStoreImpl(private val prefs: SharedPrefsManager) : MembershipStore {

    private val _hasMembership: MutableStateFlow<Boolean> =
        MutableStateFlow(prefs.getBoolean(SharedPrefsManager.KEY_HAS_MEMBERSHIP, default = false))
    override val hasMembership: StateFlow<Boolean> = _hasMembership.asStateFlow()

    private val _currentPlanKey: MutableStateFlow<String?> =
        MutableStateFlow(prefs.getString(SharedPrefsManager.KEY_CURRENT_PLAN))
    override val currentPlanKey: StateFlow<String?> = _currentPlanKey.asStateFlow()

    private val _paymentMethods: MutableStateFlow<List<PaymentMethod>> =
        MutableStateFlow(emptyList())
    override val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    override fun availablePlans(): List<MembershipPlan> = PlanCatalogue

    override fun findPlan(planKey: String): MembershipPlan? =
        PlanCatalogue.firstOrNull { it.key == planKey }

    override suspend fun addPaymentMethod(method: PaymentMethod) {
        _paymentMethods.value = _paymentMethods.value + method
    }

    override suspend fun activateMembership(planKey: String) {
        prefs.putBoolean(SharedPrefsManager.KEY_HAS_MEMBERSHIP, value = true)
        prefs.putString(SharedPrefsManager.KEY_CURRENT_PLAN, value = planKey)
        _currentPlanKey.value = planKey
        _hasMembership.value = true
    }

    override suspend fun cancelSubscription() {
        prefs.putBoolean(SharedPrefsManager.KEY_HAS_MEMBERSHIP, value = false)
        prefs.remove(SharedPrefsManager.KEY_CURRENT_PLAN)
        _currentPlanKey.value = null
        _hasMembership.value = false
    }

    /**
     * Hardcoded plan catalogue. Feature labels are intentionally English-only for the
     * payment & membership surface. Replace [availablePlans] with a Retrofit-backed call
     * once a `/plans` endpoint is available.
     */
    companion object {
        private val PlanCatalogue: List<MembershipPlan> = listOf(
            MembershipPlan(
                key = "basic",
                name = "Basic",
                monthlyPrice = "S/. 59",
                features = listOf(
                    "Basic check-in",
                    "Surveys",
                ),
                isRecommended = false,
            ),
            MembershipPlan(
                key = "pro",
                name = "Plan Pro",
                monthlyPrice = "S/. 99",
                features = listOf(
                    "Basic check-in",
                    "Surveys",
                    "Workplace forum",
                    "HR messaging",
                    "Encrypted reports",
                ),
                isRecommended = true,
            ),
        )
    }
}
