package com.elysium.softwork.shared.domain.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Runtime language switcher for the SoftWork Employee client.
 *
 * Wraps [AppCompatDelegate.setApplicationLocales], the AndroidX-recommended path for per-app
 * languages. On API 33+ this delegates to the platform `LocaleManager` and the choice surfaces
 * in System Settings → Languages. On older APIs, AppCompat back-ports the same behavior.
 *
 * Persistence is delegated to AppCompat via the `AppLocalesMetadataHolderService` declared in
 * `AndroidManifest.xml` with `autoStoreLocales=true`, so no `SharedPreferences` plumbing lives
 * here. The framework re-applies the configuration without us calling [android.app.Activity.recreate].
 *
 * Do not use [android.content.Context.createConfigurationContext] or override
 * `attachBaseContext` — those approaches are obsolete and conflict with App Bundles and
 * per-app language settings.
 */
object LocaleHelper {

    /**
     * Applies the user-selected [locale]. AppCompat persists the choice and triggers the
     * configuration update transparently.
     *
     * Pass [AppLocale.SYSTEM] to clear any per-app override and fall back to the device locale.
     */
    fun apply(locale: AppLocale) {
        val tag: String? = locale.tag
        val list: LocaleListCompat = if (tag == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(list)
    }

    /**
     * Returns the [AppLocale] currently in effect for the application, or [AppLocale.SYSTEM]
     * when no per-app override is set.
     */
    fun current(): AppLocale {
        val list: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        if (list.isEmpty) return AppLocale.SYSTEM
        val active: String = list.get(0)?.language ?: return AppLocale.SYSTEM
        return AppLocale.entries.firstOrNull { it.tag == active } ?: AppLocale.SYSTEM
    }
}
