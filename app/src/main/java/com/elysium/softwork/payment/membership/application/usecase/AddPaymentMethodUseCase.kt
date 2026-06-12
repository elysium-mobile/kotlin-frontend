package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import java.util.UUID

/**
 * Builds and persists a [PaymentMethod] from the raw card-composer inputs.
 *
 * Owns the card-assembly business rules so they cannot drift between callers:
 *  - brand detection from the PAN's leading BIN digit (mock heuristic — replace with the
 *    processor-issued brand once a real payment backend ships);
 *  - last-4 derivation, the only PAN fragment that survives past this boundary;
 *  - holder-name trimming;
 *  - local UUID identity until the backend assigns one.
 *
 * The full PAN never leaves this use case: only the derived [PaymentMethod] reaches the
 * data port, which keeps the PCI-sensitive surface as small as possible.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port that persists the saved card.
 */
class AddPaymentMethodUseCase(private val store: MembershipStore) {

    /**
     * Assembles and persists the card.
     *
     * @param holderName name embossed on the card; trimmed before persistence.
     * @param pan full card number as digits only. Used to derive brand + last4, then
     *   discarded.
     * @param expiryMonthYear expiry in the pre-formatted `MM/YY` form.
     * @return the persisted [PaymentMethod] (with its locally generated identity).
     */
    suspend operator fun invoke(
        holderName: String,
        pan: String,
        expiryMonthYear: String,
    ): PaymentMethod {
        val method = PaymentMethod(
            id = UUID.randomUUID().toString(),
            brand = detectBrand(pan),
            holderName = holderName.trim(),
            last4 = pan.takeLast(LAST4_LENGTH),
            expiryMonthYear = expiryMonthYear,
        )
        store.addPaymentMethod(method)
        return method
    }

    /**
     * Single-digit BIN heuristic labelling the card brand for the badge UI. Good enough
     * for the mock; swap with the processor's response in production.
     */
    private fun detectBrand(pan: String): String = when {
        pan.startsWith("4") -> "Visa"
        pan.startsWith("5") -> "Mastercard"
        pan.startsWith("3") -> "Amex"
        pan.startsWith("6") -> "Discover"
        else -> "Card"
    }

    private companion object {
        const val LAST4_LENGTH: Int = 4
    }
}
