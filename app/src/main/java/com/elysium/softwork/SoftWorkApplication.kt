package com.elysium.softwork

import android.app.Application
import com.elysium.softwork.shared.core.ServiceLocator

/**
 * Process-wide entry point for the SoftWork Employee client.
 *
 * Owns the [ServiceLocator] that wires shared infrastructure (SharedPreferences, Retrofit)
 * and per-context Stores. ViewModels resolve their dependencies through the locator via
 * the factory exposed on each ViewModel companion.
 *
 * Locale persistence is delegated to AppCompat (see `AppLocalesMetadataHolderService` in the
 * manifest); this class does not override `attachBaseContext`.
 */
class SoftWorkApplication : Application() {

    /** Lazy-initialized so tests/components observing process death don't pay before [onCreate]. */
    lateinit var serviceLocator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        serviceLocator = ServiceLocator(this)
    }
}
