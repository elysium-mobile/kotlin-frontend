package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore

/**
 * Activates the worker's membership for a paid plan.
 *
 * Persists the membership flag and the active plan key atomically through the data port;
 * the port's reactive `hasMembership` stream then flips, which is what unmounts the
 * payment gate at the application root.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port owning the persisted flags.
 */
class ActivateMembershipUseCase(private val store: MembershipStore) {

    /** @param planKey stable identifier of the plan the worker just paid for. */
    suspend operator fun invoke(planKey: String) {
        store.activateMembership(planKey)
    }
}
