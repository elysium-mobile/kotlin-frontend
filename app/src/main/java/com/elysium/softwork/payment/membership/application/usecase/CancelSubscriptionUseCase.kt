package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore

/**
 * Cancels the worker's active membership.
 *
 * Clears the persisted membership flags through the data port. Saved cards are
 * intentionally preserved so the worker can re-subscribe without re-entering details.
 * The port's reactive `hasMembership` stream flips to `false`, which swaps the worker
 * out of the main shell and back into the membership selection flow.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port owning the persisted flags.
 */
class CancelSubscriptionUseCase(private val store: MembershipStore) {

    /** Executes the cancellation. Idempotent. */
    suspend operator fun invoke() {
        store.cancelSubscription()
    }
}
