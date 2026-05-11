package com.elysium.softwork.shared.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Thin wrapper around the legacy [SharedPreferences] API for cross-cutting key/value storage.
 *
 * Phase 2 keeps this minimal — Stores depend on it for credentials/session tokens. We avoid
 * DataStore because the locked stack (Kotlin / Compose / Retrofit / Room) does not include it
 * and adding a new dependency for a single token write is not justified yet.
 *
 * NOTE: this stores tokens in plain shared preferences. If the security team requires
 * encryption later, swap the backing store to `EncryptedSharedPreferences` here without
 * touching call sites.
 */
class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the value for [key] or `null` when not set. */
    fun getString(key: String): String? = prefs.getString(key, null)

    /** Persists [value] under [key]. Pass `null` to clear the entry. */
    fun putString(key: String, value: String?) {
        prefs.edit {
            if (value == null) remove(key) else putString(key, value)
        }
    }

    /** Removes the entry for [key]. Idempotent. */
    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    /** Returns the boolean stored under [key] or [default] when not set. */
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)

    /** Persists [value] under [key]. */
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    companion object {
        private const val PREFS_NAME = "softwork_prefs"

        /** Storage key for the active session JWT. */
        const val KEY_AUTH_TOKEN: String = "auth_token"

        /** Storage key for the cached user id of the active session. */
        const val KEY_USER_ID: String = "user_id"

        /** Storage key for the global anonymity master switch. */
        const val KEY_GLOBAL_ANONYMITY: String = "global_anonymity"

        /** Storage key for the per-context anonymity flag in the workers' forum. */
        const val KEY_FORUM_ANONYMITY: String = "forum_anonymity"

        /** Storage key for the per-context anonymity flag in surveys. */
        const val KEY_SURVEYS_ANONYMITY: String = "surveys_anonymity"

        /** Storage key for the per-context anonymity flag in incident reports. */
        const val KEY_REPORTS_ANONYMITY: String = "reports_anonymity"
    }
}
