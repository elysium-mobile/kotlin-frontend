package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan

/**
 * Fetches the membership plan catalogue from the backend.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port serving the catalogue.
 */
class GetMembershipPlansUseCase(private val store: MembershipStore) {

    /**
     * @return [Result.success] with the plan catalogue or [Result.failure] (a `400` arrives as
     *   a [com.elysium.softwork.shared.data.network.BadRequestException]).
     */
    suspend operator fun invoke(): Result<List<MembershipPlan>> = store.getPlans()
}
