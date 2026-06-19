package com.elysium.softwork.iam.data.store

import com.elysium.softwork.iam.domain.model.User

/**
 * IAM access port. Use cases and ViewModels depend on this contract; the concrete
 * implementation in [AuthStoreImpl] orchestrates the Retrofit WebService and local
 * session storage.
 *
 * The interface returns [Result] so call sites get a single, predictable error channel —
 * HTTP failures and thrown exceptions are converted to [Result.failure] by the impl. A
 * `400 Bad Request` surfaces as a
 * [com.elysium.softwork.shared.data.network.BadRequestException] carrying the parsed
 * field-level validation payload.
 */
interface AuthStore {

    /**
     * Signs in with corporate credentials. On success the JWT, the user-account id, and the
     * credentials are persisted, and a sequential `employee-profile` lookup resolves and
     * stores the `employee_profile_id`.
     */
    suspend fun login(email: String, password: String): Result<User>

    /** Registers a new employee account (employee sign-up endpoint). */
    suspend fun register(name: String, email: String, password: String): Result<User>

    /**
     * Registers a Google-linked employee. The backend has no dedicated Google endpoint, so
     * this funnels through the **same** `sign-up/employee` path — the Google identity
     * supplies the email server-side, and only the display [name] is collected on-device.
     */
    suspend fun registerWithGoogle(name: String): Result<User>

    /**
     * Re-runs `sign-in` with the credentials persisted at the last successful login to obtain
     * a fresh token. The backend exposes no refresh endpoint, so this is the only way to renew
     * the session — invoked after a successful membership payment. Fails when no credentials
     * are stored.
     */
    suspend fun reauthenticate(): Result<User>

    /** Returns the locally-cached JWT, or `null` when no session exists. */
    fun activeToken(): String?

    /** Clears the persisted session (token, account/profile ids, and stored credentials). */
    fun clearSession()
}
