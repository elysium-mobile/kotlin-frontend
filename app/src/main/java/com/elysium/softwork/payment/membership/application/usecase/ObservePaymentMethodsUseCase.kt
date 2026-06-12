package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the worker's saved payment methods as a hot stream.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port owning the saved-cards list.
 */
class ObservePaymentMethodsUseCase(private val store: MembershipStore) {

    /** @return hot stream of the saved-cards list, re-emitting on every mutation. */
    operator fun invoke(): StateFlow<List<PaymentMethod>> = store.paymentMethods
}
