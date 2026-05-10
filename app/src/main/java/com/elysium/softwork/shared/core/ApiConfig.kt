package com.elysium.softwork.shared.core

/**
 * Process-wide API configuration. Phase 2 ships a placeholder host until the backend URL is
 * confirmed by the platform team. Replace [BASE_URL] with the production gateway when ready.
 */
object ApiConfig {
    /** Base URL consumed by Retrofit. Trailing slash is required by Retrofit's URL resolution. */
    const val BASE_URL: String = "https://api.softwork.elysium.example/"
}
