package com.elysium.softwork.shared.data.network

import com.elysium.softwork.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Process-wide Retrofit instance. Bounded contexts retrieve their typed WebService through
 * [retrofit] and call `create(MyWebService::class.java)` from their store implementation.
 *
 * Configuration sources (all compile-time, never embedded as Kotlin literals):
 *
 * - `BuildConfig.BACKEND_BASE_URL` — fed by the Secrets Gradle Plugin from
 *   `secrets.properties` (gitignored) with a fallback to `secrets.defaults.properties`
 *   (committed). The base URL is the **single source of truth**; WebService interfaces
 *   must only declare relative paths.
 * - `BuildConfig.API_KEY_GEMINI` / `API_KEY_EXTERNAL_SERVICE` — consumed by
 *   [ApiKeyInterceptor] to attach third-party credentials on a per-host basis.
 *
 * The session JWT is attached by [AuthInterceptor], which reads the live token through the
 * provider installed via [installTokenProvider]. The provider is wired by `ServiceLocator`
 * to `SharedPrefsManager.KEY_AUTH_TOKEN` before the first network call, so this object keeps
 * its no-`Context` `object` shape while still reflecting session changes per request.
 *
 * Debug builds get an [HttpLoggingInterceptor] at `BODY` level for inspecting requests in
 * Logcat; release builds omit it entirely (the dependency is `debugImplementation` only).
 */
object ApiClient {

    /**
     * Live JWT supplier consulted by [AuthInterceptor] on every authenticated request.
     * Defaults to a no-session provider so a build that forgets to wire it simply sends no
     * `Authorization` header rather than crashing. `@Volatile` so the install performed on
     * the main thread during `Application.onCreate` is visible to OkHttp's dispatcher threads.
     */
    @Volatile
    private var tokenProvider: () -> String? = { null }

    /**
     * Registers the [provider] used to resolve the current session token for outgoing
     * requests. Idempotent and cheap; call it once from `ServiceLocator` before any store
     * triggers a network call.
     */
    fun installTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .addInterceptor(
                ApiKeyInterceptor(
                    geminiApiKey = BuildConfig.API_KEY_GEMINI,
                    externalApiKey = BuildConfig.API_KEY_EXTERNAL_SERVICE,
                ),
            )
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        val baseUrl = BuildConfig.BACKEND_BASE_URL
        check(baseUrl.isNotBlank()) {
            "BACKEND_BASE_URL is blank. Copy secrets.defaults.properties to " +
                "secrets.properties and set BACKEND_BASE_URL=https://<your-backend>/ " +
                "before building."
        }
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
