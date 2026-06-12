package com.elysium.softwork.payment.membership.application.usecase

import kotlinx.coroutines.delay

/**
 * Executes the membership payment against the payment processor.
 *
 * Mocked: suspends for a fixed interval so the UI's processing state is visible, then
 * resolves successfully. Replace the body with the real processor call (idempotency key,
 * retry policy, error mapping) when the subscriptions backend ships — the suspend
 * signature already matches that contract.
 *
 * Stateless; safe to share a single instance process-wide.
 */
class PayMembershipUseCase {

    /**
     * Performs the (mocked) charge.
     *
     * @return [Result.success] always, after the simulated round-trip elapses on the
     *   caller's dispatcher. Virtual-time test schedulers fast-forward through it.
     */
    suspend operator fun invoke(): Result<Unit> {
        delay(MOCK_PAYMENT_DELAY_MS)
        return Result.success(Unit)
    }

    private companion object {
        const val MOCK_PAYMENT_DELAY_MS: Long = 1_000L
    }
}
