package com.elysium.softwork.shared.core

import android.content.Context
import com.elysium.softwork.iam.data.network.AuthWebService
import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.data.store.AuthStoreImpl
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.data.network.ApiClient

/**
 * Manual service locator. The locked stack does not include Hilt, so a single, explicit
 * locator owns the wiring of process-wide singletons: shared prefs, the Retrofit instance,
 * each context's WebService, and each context's Store implementation.
 *
 * Stores are exposed as their interface type ([AuthStore], future `PostStore`, etc.) so
 * call sites depend on the contract rather than the impl. New bounded contexts add their
 * stores here as they come online.
 */
class ServiceLocator(context: Context) {

    // region Shared infrastructure
    val sharedPrefsManager: SharedPrefsManager = SharedPrefsManager(context)
    // endregion

    // region IAM
    private val authWebService: AuthWebService =
        ApiClient.retrofit.create(AuthWebService::class.java)

    val authStore: AuthStore = AuthStoreImpl(authWebService, sharedPrefsManager)
    // endregion
}
