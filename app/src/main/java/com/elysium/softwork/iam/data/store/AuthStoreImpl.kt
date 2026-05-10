package com.elysium.softwork.iam.data.store

import com.elysium.softwork.iam.data.network.AuthWebService
import com.elysium.softwork.iam.domain.model.User
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import retrofit2.Response

/**
 * Concrete [AuthStore]. Orchestrates [AuthWebService] calls and persists the JWT in
 * [SharedPrefsManager] so the session survives process death.
 *
 * Each public method funnels its work through [callAndPersist], which:
 * 1. Catches network/HTTP failures and surfaces them as [Result.failure].
 * 2. Persists the returned token (when present) to local storage.
 *
 * This keeps the per-endpoint methods declarative.
 */
class AuthStoreImpl(
    private val webService: AuthWebService,
    private val prefs: SharedPrefsManager,
) : AuthStore {

    override suspend fun login(email: String, password: String): Result<User> =
        callAndPersist { webService.login(User(email = email, password = password)) }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        role: String,
    ): Result<User> = callAndPersist {
        webService.register(
            User(username = username, email = email, password = password, role = role),
        )
    }

    override suspend fun registerWithGoogle(username: String, role: String): Result<User> =
        callAndPersist { webService.registerWithGoogle(User(username = username, role = role)) }

    override fun activeToken(): String? = prefs.getString(SharedPrefsManager.KEY_AUTH_TOKEN)

    override fun clearSession() {
        prefs.remove(SharedPrefsManager.KEY_AUTH_TOKEN)
        prefs.remove(SharedPrefsManager.KEY_USER_ID)
    }

    /**
     * Executes [block], unwraps the [Response], and writes the token + user id to local
     * storage when the call succeeds. HTTP errors and thrown exceptions become
     * [Result.failure].
     */
    private suspend inline fun callAndPersist(crossinline block: suspend () -> Response<User>): Result<User> =
        runCatching {
            val response = block()
            if (!response.isSuccessful) {
                error("HTTP ${response.code()} ${response.message().ifBlank { "request failed" }}")
            }
            val body = response.body() ?: error("Empty response body")
            body.token?.let { prefs.putString(SharedPrefsManager.KEY_AUTH_TOKEN, it) }
            body.id?.let { prefs.putString(SharedPrefsManager.KEY_USER_ID, it) }
            body
        }
}
