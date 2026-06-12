package com.elysium.softwork.iam.application.usecase

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * Signs the worker in with corporate credentials.
 *
 * Owns the input-normalization business rule (the email is trimmed before it reaches the
 * data port) so every caller — ViewModel, future deep-link handler, test — gets identical
 * behavior. The password is forwarded verbatim because whitespace may be significant.
 *
 * Stateless and allocation-free per call; safe to share a single instance process-wide.
 *
 * @param store IAM data port that performs the network call and persists the session.
 */
class LoginUseCase(private val store: AuthStore) {

    /**
     * Executes the login.
     *
     * @param email corporate email as typed by the worker; trimmed before dispatch.
     * @param password plain-text password, forwarded without modification.
     * @return [Result.success] with the resolved [User] (session already persisted by the
     *   data layer) or [Result.failure] carrying the transport error.
     */
    suspend operator fun invoke(email: String, password: String): Result<User> =
        store.login(email.trim(), password)
}
