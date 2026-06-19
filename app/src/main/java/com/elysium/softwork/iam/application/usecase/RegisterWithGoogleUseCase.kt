package com.elysium.softwork.iam.application.usecase

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * Registers a Google-linked employee account through the `sign-up/employee` endpoint.
 *
 * The backend exposes no dedicated Google route, so the Gmail registration path reuses the
 * standard employee sign-up — the Google identity resolves the email server-side and only
 * the display name is collected on-device. This use case owns the input-normalization rule
 * (the name is trimmed before it reaches the data port).
 *
 * Stateless and allocation-free per call; safe to share a single instance process-wide. The
 * client is employee-exclusive, so there is no role parameter.
 *
 * @param store IAM data port that performs the network call and persists the session.
 */
class RegisterWithGoogleUseCase(private val store: AuthStore) {

    /**
     * Executes the Google-linked registration.
     *
     * @param name display name; trimmed before dispatch.
     * @return [Result.success] with the created [User] or [Result.failure] on error.
     */
    suspend operator fun invoke(name: String): Result<User> =
        store.registerWithGoogle(name = name.trim())
}
