package com.elysium.softwork.iam.data.store

import com.elysium.softwork.iam.domain.model.User

/**
 * IAM access port. Use cases and ViewModels depend on this contract; the concrete
 * implementation in [AuthStoreImpl] orchestrates the Retrofit WebService and local
 * token storage.
 *
 * The interface returns [Result] so call sites get a single, predictable error channel —
 * exceptions thrown by the network layer are converted to [Result.failure] by the impl.
 */
interface AuthStore {

    /** Sign-in with corporate credentials. On success the JWT is persisted locally. */
    suspend fun login(email: String, password: String): Result<User>

    /** Standard registration flow (corporate email + password). */
    suspend fun register(username: String, email: String, password: String, role: String): Result<User>

    /** Registration flow for accounts that authenticated via Google (no password collected). */
    suspend fun registerWithGoogle(username: String, role: String): Result<User>

    /** Returns the locally-cached JWT, or `null` when no session exists. */
    fun activeToken(): String?

    /** Clears the persisted session (token + cached user id). */
    fun clearSession()
}
