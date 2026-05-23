package com.elysium.softwork.payment.membership.domain.model

import com.google.gson.annotations.SerializedName

/**
 * A saved payment instrument associated with the worker's profile.
 *
 * The same instance flows through the Retrofit web service request/response and into the
 * in-memory catalogue, so all fields default to empty.
 *
 * SECURITY: this class describes a tokenized reference returned by the payment processor.
 * It is never used to carry a raw PAN — only [last4] is safe to display, and even that is
 * derived from the user-entered digits before persistence. Treat the type as PCI-sensitive
 * regardless of storage backend.
 *
 * @property id stable identifier issued by the backend (or a generated UUID in the mock
 *   store).
 * @property brand card brand label (e.g. "Visa", "Mastercard"). Derived from the PAN's BIN
 *   prefix when the user adds a card.
 * @property holderName name embossed on the card, as entered by the worker.
 * @property last4 the last four digits of the PAN, the only portion safe to display.
 * @property expiryMonthYear expiry in the user-entered `MM/YY` form (e.g. "01/27").
 */
data class PaymentMethod(
    @SerializedName("id") val id: String = "",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("holderName") val holderName: String = "",
    @SerializedName("last4") val last4: String = "",
    @SerializedName("expiryMonthYear") val expiryMonthYear: String = "",
)
