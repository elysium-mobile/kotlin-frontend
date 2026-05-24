package com.elysium.softwork.testsupport

import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User

/**
 * In-memory test double for [AuthStore].
 *
 * Designed for ViewModel unit tests where the production Retrofit-backed implementation
 * would require a live network or a mock web server. The fake is fully deterministic —
 * every call returns the value programmed via the public `next*` properties — and records
 * the arguments of the last successful invocation so tests can assert that the ViewModel
 * forwarded the right values to the store.
 *
 * The class is `open` so individual tests can override a method to inject delays, throw
 * exceptions during the suspend call, or simulate concurrent invocations without rewriting
 * the whole double.
 *
 * Manual DI fit: pass an instance of this fake straight into a ViewModel constructor.
 * The production code never reaches for the `ServiceLocator` once the ViewModel is
 * instantiated, so swapping the store implementation here is a complete isolation
 * boundary — no Hilt, no reflection, no Robolectric.
 *
 * @property nextLoginResult value returned by the next [login] call.
 * @property nextRegisterResult value returned by the next [register] call.
 * @property nextRegisterWithGoogleResult value returned by the next [registerWithGoogle] call.
 */
open class FakeAuthStore(
    var nextLoginResult: Result<User> = Result.success(DEFAULT_USER),
    var nextRegisterResult: Result<User> = Result.success(DEFAULT_USER),
    var nextRegisterWithGoogleResult: Result<User> = Result.success(DEFAULT_USER),
) : AuthStore {

    /** Number of times each method has been invoked. Useful for "called exactly once" assertions. */
    var loginInvocations: Int = 0
        private set

    var registerInvocations: Int = 0
        private set

    var registerWithGoogleInvocations: Int = 0
        private set

    var clearSessionInvocations: Int = 0
        private set

    /** Arguments of the most recent [login] call, or `null` when [login] has never been invoked. */
    var lastLoginArgs: Pair<String, String>? = null
        private set

    /** Arguments of the most recent [register] call. */
    var lastRegisterArgs: RegisterArgs? = null
        private set

    /** Arguments of the most recent [registerWithGoogle] call. */
    var lastRegisterWithGoogleArgs: Pair<String, String>? = null
        private set

    /** Token returned by [activeToken]. Defaults to `null` so the auth gate routes to LOGIN. */
    var storedToken: String? = null

    override suspend fun login(email: String, password: String): Result<User> {
        loginInvocations += 1
        lastLoginArgs = email to password
        return nextLoginResult
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        role: String,
    ): Result<User> {
        registerInvocations += 1
        lastRegisterArgs = RegisterArgs(username, email, password, role)
        return nextRegisterResult
    }

    override suspend fun registerWithGoogle(username: String, role: String): Result<User> {
        registerWithGoogleInvocations += 1
        lastRegisterWithGoogleArgs = username to role
        return nextRegisterWithGoogleResult
    }

    override fun activeToken(): String? = storedToken

    override fun clearSession() {
        clearSessionInvocations += 1
        storedToken = null
    }

    /** Captured argument tuple for [register], grouped so assertions stay legible. */
    data class RegisterArgs(
        val username: String,
        val email: String,
        val password: String,
        val role: String,
    )

    companion object {
        /** Default user surfaced on success unless a test overrides [nextLoginResult] etc. */
        val DEFAULT_USER: User = User(
            id = "test-user",
            username = "test",
            email = "test@elysium.com",
            role = "EMPLOYEE",
            token = "TEST_TOKEN",
        )
    }
}
