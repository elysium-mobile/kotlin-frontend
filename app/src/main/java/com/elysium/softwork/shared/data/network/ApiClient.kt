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
 * Debug builds get an [HttpLoggingInterceptor] at `BODY` level for inspecting requests in
 * Logcat; release builds omit it entirely (the dependency is `debugImplementation` only).
 */
object ApiClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
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
