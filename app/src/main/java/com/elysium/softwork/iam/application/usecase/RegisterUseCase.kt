package com.elysium.softwork.iam.application.usecase

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * Registers a new employee account against the `sign-up/employee` endpoint.
 *
 * Owns the input-normalization business rule: the display name and email are trimmed before
 * they reach the data port. Field-level validation (email format, corporate domain, password
 * strength) stays in `AuthValidation` and is enforced by the caller before invoking this use
 * case — the use case assumes its inputs already passed those gates.
 *
 * Stateless and allocation-free per call; safe to share a single instance process-wide. The
 * client is employee-exclusive, so there is no role parameter — the endpoint itself scopes
 * the account to the employee experience.
 *
 * @param store IAM data port that performs the network call and persists the session.
 */
class RegisterUseCase(private val store: AuthStore) {

    /**
     * Executes the registration.
     *
     * @param name display name sent as the worker's first name; trimmed before dispatch.
     * @param email corporate email; trimmed before dispatch.
     * @param password plain-text password, forwarded without modification.
     * @return [Result.success] with the created [User] or [Result.failure] on error.
     */
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> =
        store.register(name = name.trim(), email = email.trim(), password = password)
}
