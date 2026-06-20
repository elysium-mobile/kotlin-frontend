package com.elysium.softwork.payment.membership.domain.model

/**
 * A payment record settling an [Order] — the annotation-free bean for the `payments`
 * endpoints (the *Bean / Pragmatic Shortcut*).
 *
 * Property names match the backend wire keys exactly so Gson resolves them by reflection
 * without `@SerializedName`. The order id, transaction id, and date arrive under camelCase
 * keys on the request and snake_case on the response, so both spellings coexist as nullable
 * fields.
 *
 * @property payment_id primary key returned by every payment response.
 * @property orderId settled order on the **create request** (`orderId`).
 * @property order_id settled order on the **response** (`order_id`).
 * @property transactionId processor transaction id on the **create request** (`transactionId`).
 * @property transaction_id processor transaction id on the **response** (`transaction_id`).
 * @property paymentDate payment date on the **create request** (`paymentDate`).
 * @property payment_date payment date on the **response** (`payment_date`).
 */
data class Payment(
    val payment_id: Long? = null,
    val orderId: Long? = null,
    val order_id: Long? = null,
    val transactionId: String? = null,
    val transaction_id: String? = null,
    val paymentDate: String? = null,
    val payment_date: String? = null,
)
