package com.elysium.softwork.payment.membership.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Orchestrates the payment & membership flow.
 *
 * One instance backs the four screens of the payment graph. State is split into three
 * independent streams:
 * - [paymentMethods] — proxied straight from the store so additions reflect everywhere.
 * - [cardForm] — local buffer for the credit-card composer.
 * - [paymentState] — lifecycle of the "Pay membership" action ([PaymentState]).
 *
 * Membership activation / cancellation delegate to the store so the reactive flip on
 * `MembershipStore.hasMembership` propagates to `MainActivity` and triggers the shell swap.
 */
class MembershipViewModel(private val store: MembershipStore) : ViewModel() {

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

    /** Catalogue of plans the user can choose from. Read-only snapshot from the store. */
    val availablePlans: List<MembershipPlan> = store.availablePlans()

    /** Live list of saved cards. Backed by the store's in-memory `StateFlow`. */
    val paymentMethods: StateFlow<List<PaymentMethod>> = store.paymentMethods

    /**
     * Stable identifier of the currently active plan, or `null` when no membership is
     * active. Surfaced from the store so `PaymentMethodsScreen` can resolve the active plan
     * when entered from settings (where the nav arg is the `CURRENT_PLAN_SENTINEL`).
     */
    val currentPlanKey: StateFlow<String?> = store.currentPlanKey

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
     * Validates the form and pushes a new [PaymentMethod] into the store when the
     * "Save this card" switch is on. The form is cleared on success so a subsequent visit
     * to the composer starts fresh.
     *
     * @param onAdded callback invoked after the card has been persisted, so the caller can
     *   pop back to the methods screen on the main thread.
     */
    fun addCard(onAdded: () -> Unit) {
        val current: CardFormState = _cardForm.value
        if (!current.isValid) return
        viewModelScope.launch {
            if (current.saveCard) {
                store.addPaymentMethod(
                    PaymentMethod(
                        id = UUID.randomUUID().toString(),
                        brand = detectBrand(current.cardNumber),
                        holderName = current.holderName.trim(),
                        last4 = current.cardNumber.takeLast(LAST4_LENGTH),
                        expiryMonthYear = current.expiry,
                    ),
                )
            }
            _cardForm.value = CardFormState()
            onAdded()
        }
    }

    /**
     * Simulates the payment call. Triggers a [MOCK_PAYMENT_DELAY_MS] delay so the spinner
     * is visible, then flips [paymentState] to [PaymentState.Succeeded] — the screen
     * observes that and navigates to `PaymentSuccessScreen`.
     */
    fun payMembership() {
        if (_paymentState.value is PaymentState.Processing) return
        _paymentState.value = PaymentState.Processing
        viewModelScope.launch {
            delay(MOCK_PAYMENT_DELAY_MS)
            _paymentState.value = PaymentState.Succeeded
        }
    }

    /**
     * Activates the worker's membership against the store using [planKey]. The store flips
     * its reactive `hasMembership` flag, which `MainActivity` observes and uses to swap
     * the user from the payment graph into the main app shell.
     */
    fun activateMembership(planKey: String) {
        viewModelScope.launch { store.activateMembership(planKey) }
    }

    /**
     * Resets [paymentState] back to [PaymentState.Idle]. Called from the methods screen on
     * dispose so a subsequent visit does not auto-navigate based on a stale flag.
     */
    fun consumePaymentState() {
        _paymentState.value = PaymentState.Idle
    }

    /**
     * Immediately cancels the active membership. The store clears
     * `KEY_HAS_MEMBERSHIP` / `KEY_CURRENT_PLAN` and emits the new state; the host Activity
     * picks it up and unmounts the main shell in favour of the payment graph rooted at the
     * selection screen. Card data is intentionally preserved by the store.
     */
    fun cancelSubscription() {
        viewModelScope.launch { store.cancelSubscription() }
    }
    // endregion

    /**
     * Crude BIN-prefix heuristic used by the mock to label the card. Replace with the
     * processor-issued brand string when the real `/payment-methods` endpoint ships.
     */
    private fun detectBrand(pan: String): String = when {
        pan.startsWith("4") -> "Visa"
        pan.startsWith("5") -> "Mastercard"
        pan.startsWith("3") -> "Amex"
        pan.startsWith("6") -> "Discover"
        else -> "Card"
    }

    companion object {
        private const val MIN_PAN_LENGTH: Int = 13
        private const val MAX_PAN_LENGTH: Int = 19
        private const val EXPIRY_LENGTH: Int = 5
        private const val EXPIRY_DIGITS: Int = 4
        private const val MIN_CVV_LENGTH: Int = 3
        private const val MAX_CVV_LENGTH: Int = 4
        private const val LAST4_LENGTH: Int = 4
        private const val MOCK_PAYMENT_DELAY_MS: Long = 1_000L

        /**
         * Factory that pulls the [MembershipStore] from the [SoftWorkApplication] service
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
                return MembershipViewModel(application.serviceLocator.membershipStore) as T
            }
        }
    }
}
