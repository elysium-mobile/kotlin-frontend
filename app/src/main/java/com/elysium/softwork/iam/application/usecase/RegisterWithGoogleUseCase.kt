package com.elysium.softwork.iam.application.usecase

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * Registers a new worker account through the Google identity flow (no password collected).
 *
 * Owns the input-normalization business rule: the username is trimmed before it reaches
 * the data port.
 *
 * Stateless and allocation-free per call; safe to share a single instance process-wide.
 *
 * @param store IAM data port that performs the network call and persists the session.
 */
class RegisterWithGoogleUseCase(private val store: AuthStore) {

    /**
     * Executes the Google-flow registration.
     *
     * @param username display name; trimmed before dispatch.
     * @param role business role string emitted by this client (always `"EMPLOYEE"`).
     * @return [Result.success] with the created [User] or [Result.failure] on error.
     */
    suspend operator fun invoke(username: String, role: String): Result<User> =
        store.registerWithGoogle(username = username.trim(), role = role)
}
