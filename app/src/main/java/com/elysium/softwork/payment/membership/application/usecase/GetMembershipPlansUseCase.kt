package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan

/**
 * Reads the membership plan catalogue.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port serving the catalogue.
 */
class GetMembershipPlansUseCase(private val store: MembershipStore) {

    /** @return the full plan catalogue, eager because the selection screen renders it all. */
    operator fun invoke(): List<MembershipPlan> = store.availablePlans()

    /**
     * Resolves a single plan by its stable key.
     *
     * @param planKey stable plan identifier.
     * @return the matching [MembershipPlan], or `null` when no plan carries the key.
     */
    fun find(planKey: String): MembershipPlan? = store.findPlan(planKey)
}
