package com.elysium.softwork.shared.data.network

import com.elysium.softwork.shared.core.ApiConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Process-wide Retrofit instance. Bounded contexts retrieve their typed WebService through
 * [retrofit] and call `create(MyWebService::class.java)` from their store implementation.
 *
 * Kept intentionally minimal — no OkHttp customization, no auth interceptor yet. Those layers
 * will be added when the backend is ready to enforce a session header contract.
 */
object ApiClient {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
