package com.elysium.softwork.shared.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that attaches the session JWT to every authenticated request.
 *
 * The token is read **per request** through [tokenProvider] (not captured once) so the very
 * next call after a fresh `sign-in` — including the sequential employee-profile lookup —
 * already carries the new credential, and a logout that clears the token immediately stops
 * authorizing outgoing traffic.
 *
 * Public authentication endpoints are skipped: `sign-in` and `sign-up/employee` are reachable
 * without a token (the worker has none yet), so sending a stale/blank `Authorization` header
 * there is at best noise and at worst a 401 from an over-strict gateway. Matching is by path
 * suffix so it is independent of the configured `BACKEND_BASE_URL` host/prefix.
 *
 * @param tokenProvider supplies the current JWT (or `null`/blank when no session exists).
 *   Wired by `ServiceLocator` to read `SharedPrefsManager.KEY_AUTH_TOKEN`.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (isPublicEndpoint(request.url.encodedPath)) {
            return chain.proceed(request)
        }

        val token: String? = tokenProvider()
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authorized = request.newBuilder()
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
            .build()
        return chain.proceed(authorized)
    }

    /** `true` when [path] targets one of the token-free authentication endpoints. */
    private fun isPublicEndpoint(path: String): Boolean =
        PUBLIC_ENDPOINT_SUFFIXES.any { path.endsWith(it) }

    companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "

        /**
         * Path suffixes that must never receive an `Authorization` header. Kept as suffixes
         * so the check holds regardless of the API version prefix in `BACKEND_BASE_URL`.
         */
        private val PUBLIC_ENDPOINT_SUFFIXES: List<String> = listOf(
            "/authentication/sign-in",
            "/authentication/sign-up/employee",
        )
    }
}
