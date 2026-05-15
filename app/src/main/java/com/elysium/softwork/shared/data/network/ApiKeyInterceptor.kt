package com.elysium.softwork.shared.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp [Interceptor] that injects third-party API keys into outgoing requests based on
 * the destination host. Keeps API keys out of the WebService interfaces and out of source
 * control — values arrive via `BuildConfig` and are wired up at construction time.
 *
 * Two design rules enforced here:
 *
 * 1. **Host whitelist**, never path matching. The key for `generativelanguage.googleapis.com`
 *    is only ever sent to that host, never leaked to the SoftWork backend or anywhere else.
 * 2. **Blank-skip**. When the corresponding `BuildConfig` field is blank (a new dev who
 *    hasn't created `secrets.properties`, a CI environment without the secret available),
 *    no header is added and the request is forwarded untouched. This keeps the build green
 *    and surfaces the misconfiguration as a 401/403 at runtime rather than a malformed
 *    `Authorization: Bearer ` header.
 *
 * Header naming follows each provider's documented convention. Add a new entry to
 * [hostKeyMap] when a new third-party integration lands; do **not** hardcode the key in
 * the corresponding `WebService` interface.
 *
 * @param geminiApiKey Value of `BuildConfig.API_KEY_GEMINI`.
 * @param externalApiKey Value of `BuildConfig.API_KEY_EXTERNAL_SERVICE`.
 */
class ApiKeyInterceptor(
    private val geminiApiKey: String,
    private val externalApiKey: String,
) : Interceptor {

    /**
     * Map of host → (headerName, headerValue). A request whose URL host matches a key in
     * this map will receive the corresponding header. Entries whose value is blank are
     * ignored (see class docs).
     */
    private val hostKeyMap: Map<String, Pair<String, String>> = buildMap {
        if (geminiApiKey.isConfigured()) {
            put("generativelanguage.googleapis.com", HEADER_GEMINI to geminiApiKey)
        }
        if (externalApiKey.isConfigured()) {
            // Replace with the actual third-party host when the integration is wired up.
            put(EXTERNAL_HOST_PLACEHOLDER, HEADER_GENERIC_API_KEY to externalApiKey)
        }
    }

    /**
     * `true` when this string contains a real key, not a default placeholder. Tracks the
     * sentinel used by `secrets.defaults.properties`.
     */
    private fun String.isConfigured(): Boolean =
        isNotBlank() && this != UNSET_SENTINEL

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val match = hostKeyMap[request.url.host] ?: return chain.proceed(request)
        val (headerName, headerValue) = match
        val authorized = request.newBuilder()
            .addHeader(headerName, headerValue)
            .build()
        return chain.proceed(authorized)
    }

    companion object {
        private const val HEADER_GEMINI = "x-goog-api-key"
        private const val HEADER_GENERIC_API_KEY = "X-Api-Key"

        // Sentinel host that never resolves in practice. Replace with the real third-party
        // host when an integration that uses API_KEY_EXTERNAL_SERVICE is added.
        private const val EXTERNAL_HOST_PLACEHOLDER = "external.invalid"

        // Matches the placeholder in `secrets.defaults.properties`.
        private const val UNSET_SENTINEL = "unset"
    }
}
