package com.elysium.softwork.payment.membership.application.usecase

import com.elysium.softwork.payment.membership.data.store.MembershipStore
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the stable key of the active membership plan as a hot stream.
 *
 * Emits `null` while no membership is active. Consumers use it to resolve the active
 * plan when the navigation argument carries the current-plan sentinel instead of a
 * concrete key (the settings entry path).
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param store membership data port owning the membership flags.
 */
class ObserveCurrentPlanUseCase(private val store: MembershipStore) {

    /** @return hot stream of the active plan key, or `null` when unsubscribed. */
    operator fun invoke(): StateFlow<String?> = store.currentPlanKey
}
