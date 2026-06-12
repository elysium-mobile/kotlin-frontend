package com.elysium.softwork.iam.application.usecase

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * Registers a new worker account with corporate credentials.
 *
 * Owns the input-normalization business rule: username and email are trimmed before they
 * reach the data port. Field-level validation (email format, corporate domain, password
 * strength) stays in `AuthValidation` and is enforced by the caller before invoking this
 * use case — the use case assumes its inputs already passed those gates.
 *
 * Stateless and allocation-free per call; safe to share a single instance process-wide.
 *
 * @param store IAM data port that performs the network call and persists the session.
 */
class RegisterUseCase(private val store: AuthStore) {

    /**
     * Executes the registration.
     *
     * @param username display name; trimmed before dispatch.
     * @param email corporate email; trimmed before dispatch.
     * @param password plain-text password, forwarded without modification.
     * @param role business role string emitted by this client (always `"EMPLOYEE"`).
     * @return [Result.success] with the created [User] or [Result.failure] on error.
     */
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        role: String,
    ): Result<User> = store.register(
        username = username.trim(),
        email = email.trim(),
        password = password,
        role = role,
    )
}
