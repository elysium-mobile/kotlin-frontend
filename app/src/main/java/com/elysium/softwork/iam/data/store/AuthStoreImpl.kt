package com.elysium.softwork.iam.data.store

import com.elysium.softwork.iam.data.network.AuthWebService
import com.elysium.softwork.iam.domain.model.User
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import kotlinx.coroutines.delay
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
 *
 * **Testing phase note:** [login] is currently mocked — it bypasses the WebService entirely,
 * simulates a 1 s round-trip, and returns a fixed [User] + [MOCK_TOKEN]. Switch back to
 * `callAndPersist { webService.login(...) }` when the backend is reachable.
 */
class AuthStoreImpl(
    private val webService: AuthWebService,
    private val prefs: SharedPrefsManager,
) : AuthStore {

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        delay(MOCK_DELAY_MS)
        val user = User(
            id = MOCK_USER_ID,
            username = MOCK_USERNAME,
            email = email.ifBlank { MOCK_EMAIL },
            role = MOCK_ROLE,
            token = MOCK_TOKEN,
        )
        prefs.putString(SharedPrefsManager.KEY_AUTH_TOKEN, MOCK_TOKEN)
        prefs.putString(SharedPrefsManager.KEY_USER_ID, MOCK_USER_ID)
        user
    }

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

    private companion object {
        // Mock-auth constants — delete this block (and restore the original `callAndPersist`
        // call in [login]) when the real backend is wired up.
        const val MOCK_DELAY_MS: Long = 1_000L
        const val MOCK_TOKEN: String = "MOCK_TOKEN_123"
        const val MOCK_USER_ID: String = "1"
        const val MOCK_USERNAME: String = "Cesar"
        const val MOCK_EMAIL: String = "cesar@gmail.com"
        const val MOCK_ROLE: String = "EMPLOYEE"
    }
}
