package com.elysium.softwork.payment.membership.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.payment.membership.application.usecase.ActivateMembershipUseCase
import com.elysium.softwork.payment.membership.application.usecase.AddPaymentMethodUseCase
import com.elysium.softwork.payment.membership.application.usecase.CancelSubscriptionUseCase
import com.elysium.softwork.payment.membership.application.usecase.GetMembershipPlansUseCase
import com.elysium.softwork.payment.membership.application.usecase.ObserveCurrentPlanUseCase
import com.elysium.softwork.payment.membership.application.usecase.ObservePaymentMethodsUseCase
import com.elysium.softwork.payment.membership.application.usecase.PayMembershipUseCase
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the payment and membership flow.
 *
 * One instance backs the four screens of the payment graph. The ViewModel owns no
 * business logic: plan resolution, card assembly (brand detection, last-4 derivation),
 * the payment round-trip, and the membership flag mutations are all delegated to
 * application-layer use cases. What remains here is strictly UI state:
 * - [paymentMethods] / [currentPlanKey] — hot streams proxied from the observe use cases.
 * - [cardForm] — keystroke buffer for the credit-card composer, including the input
 *   formatting rules (digit filtering, `MM/YY` auto-slash) that exist purely to keep the
 *   rendered text stable.
 * - [paymentState] — lifecycle of the "Pay membership" action ([PaymentState]).
 *
 * @param getPlans reads the plan catalogue and resolves plans by key.
 * @param observePaymentMethods streams the saved-cards list.
 * @param observeCurrentPlan streams the active plan key.
 * @param addPaymentMethod assembles and persists a card from raw composer input.
 * @param payMembership executes the (mocked) charge.
 * @param activateMembership flips the persisted membership gate after payment.
 * @param cancelSubscription clears the persisted membership gate.
 */
class MembershipViewModel(
    getPlans: GetMembershipPlansUseCase,
    observePaymentMethods: ObservePaymentMethodsUseCase,
    observeCurrentPlan: ObserveCurrentPlanUseCase,
    private val addPaymentMethod: AddPaymentMethodUseCase,
    private val payMembership: PayMembershipUseCase,
    private val activateMembership: ActivateMembershipUseCase,
    private val cancelSubscription: CancelSubscriptionUseCase,
) : ViewModel() {

    /** Snapshot of the credit-card composer. Updated via the `on*Change` handlers. */
    data class CardFormState(
        val holderName: String = "",
        val cardNumber: String = "",
        val expiry: String = "",
        val cvv: String = "",
        val saveCard: Boolean = true,
    ) {
        /**
         * Minimal validation gate for the "Add card" button. Production should swap this
         * for a real validator (Luhn check on the PAN, expiry not in the past, CVV length
         * matching the brand, etc.).
         */
        val isValid: Boolean
            get() = holderName.isNotBlank() &&
                cardNumber.filter { it.isDigit() }.length in MIN_PAN_LENGTH..MAX_PAN_LENGTH &&
                expiry.length == EXPIRY_LENGTH &&
                cvv.length in MIN_CVV_LENGTH..MAX_CVV_LENGTH
    }

    /** Lifecycle of the "Pay membership" action. */
    sealed interface PaymentState {
        data object Idle : PaymentState
        data object Processing : PaymentState
        data object Succeeded : PaymentState
    }

    /** Catalogue of plans the user can choose from. Read-only snapshot. */
    val availablePlans: List<MembershipPlan> = getPlans()

    /** Live list of saved cards. */
    val paymentMethods: StateFlow<List<PaymentMethod>> = observePaymentMethods()

    /**
     * Stable identifier of the currently active plan, or `null` when no membership is
     * active. Used to resolve the active plan when the navigation argument carries the
     * current-plan sentinel (settings entry).
     */
    val currentPlanKey: StateFlow<String?> = observeCurrentPlan()

    private val _cardForm: MutableStateFlow<CardFormState> = MutableStateFlow(CardFormState())
    val cardForm: StateFlow<CardFormState> = _cardForm.asStateFlow()

    private val _paymentState: MutableStateFlow<PaymentState> = MutableStateFlow(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    // region Card form handlers
    /** Updates the cardholder name. */
    fun onHolderNameChange(value: String) {
        _cardForm.value = _cardForm.value.copy(holderName = value)
    }

    /**
     * Updates the card number. Filters out non-digits and caps the length at [MAX_PAN_LENGTH]
     * so the UI cannot accumulate garbage from paste events.
     */
    fun onCardNumberChange(value: String) {
        val digits: String = value.filter { it.isDigit() }.take(MAX_PAN_LENGTH)
        _cardForm.value = _cardForm.value.copy(cardNumber = digits)
    }

    /**
     * Updates the expiry. Accepts the user-friendly `MM/YY` form; non-digits are stripped
     * and the slash is re-inserted after the second digit so the user only types numbers.
     */
    fun onExpiryChange(value: String) {
        val digits: String = value.filter { it.isDigit() }.take(EXPIRY_DIGITS)
        val formatted: String = if (digits.length >= 3) {
            "${digits.substring(0, 2)}/${digits.substring(2)}"
        } else {
            digits
        }
        _cardForm.value = _cardForm.value.copy(expiry = formatted)
    }

    /** Updates the CVV. Strips non-digits and caps at [MAX_CVV_LENGTH]. */
    fun onCvvChange(value: String) {
        val digits: String = value.filter { it.isDigit() }.take(MAX_CVV_LENGTH)
        _cardForm.value = _cardForm.value.copy(cvv = digits)
    }

    /** Toggles the "Save this card" switch. */
    fun onSaveCardChange(value: Boolean) {
        _cardForm.value = _cardForm.value.copy(saveCard = value)
    }
    // endregion

    // region Actions
    /**
     * Validates the form and delegates card assembly + persistence to the use case when
     * the "Save this card" switch is on. The form buffer is cleared on success so a
     * subsequent visit to the composer starts fresh.
     *
     * @param onAdded callback invoked after the operation completes, so the caller can
     *   pop back to the methods screen on the main thread.
     */
    fun addCard(onAdded: () -> Unit) {
        val current: CardFormState = _cardForm.value
        if (!current.isValid) return
        viewModelScope.launch {
            if (current.saveCard) {
                addPaymentMethod(
                    holderName = current.holderName,
                    pan = current.cardNumber,
                    expiryMonthYear = current.expiry,
                )
            }
            _cardForm.value = CardFormState()
            onAdded()
        }
    }

    /**
     * Executes the payment through the use case. [paymentState] flips to
     * [PaymentState.Processing] immediately and to [PaymentState.Succeeded] when the
     * charge resolves — the methods screen observes that and navigates forward.
     * Re-entrant calls while processing are dropped.
     */
    fun payMembership() {
        if (_paymentState.value is PaymentState.Processing) return
        _paymentState.value = PaymentState.Processing
        viewModelScope.launch {
            payMembership.invoke()
            _paymentState.value = PaymentState.Succeeded
        }
    }

    /**
     * Activates the worker's membership for [planKey]. The persisted gate flips, which
     * the application root observes to swap the worker into the main shell.
     */
    fun activateMembership(planKey: String) {
        viewModelScope.launch { activateMembership.invoke(planKey) }
    }

    /**
     * Resets [paymentState] back to [PaymentState.Idle]. Called from the methods screen
     * after consuming a success so a later visit does not auto-navigate on a stale flag.
     */
    fun consumePaymentState() {
        _paymentState.value = PaymentState.Idle
    }

    /**
     * Cancels the active membership through the use case. The persisted gate clears and
     * the application root swaps the worker back to the membership selection flow.
     * Saved cards are preserved by the data layer.
     */
    fun cancelSubscription() {
        viewModelScope.launch { cancelSubscription.invoke() }
    }
    // endregion

    companion object {
        private const val MIN_PAN_LENGTH: Int = 13
        private const val MAX_PAN_LENGTH: Int = 19
        private const val EXPIRY_LENGTH: Int = 5
        private const val EXPIRY_DIGITS: Int = 4
        private const val MIN_CVV_LENGTH: Int = 3
        private const val MAX_CVV_LENGTH: Int = 4

        /**
         * Factory that assembles the membership use cases from the application service
         * locator.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: MembershipViewModel =
         *     viewModel(factory = MembershipViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SoftWorkApplication
                val store = application.serviceLocator.membershipStore
                return MembershipViewModel(
                    getPlans = GetMembershipPlansUseCase(store),
                    observePaymentMethods = ObservePaymentMethodsUseCase(store),
                    observeCurrentPlan = ObserveCurrentPlanUseCase(store),
                    addPaymentMethod = AddPaymentMethodUseCase(store),
                    payMembership = PayMembershipUseCase(),
                    activateMembership = ActivateMembershipUseCase(store),
                    cancelSubscription = CancelSubscriptionUseCase(store),
                ) as T
            }
        }
    }
}
