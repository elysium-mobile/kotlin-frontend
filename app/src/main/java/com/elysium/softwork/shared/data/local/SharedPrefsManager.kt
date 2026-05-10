package com.elysium.softwork.shared.data.local

import android.content.Context
import android.content.SharedPreferences

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
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    /** Removes the entry for [key]. Idempotent. */
    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        private const val PREFS_NAME = "softwork_prefs"

        /** Storage key for the active session JWT. */
        const val KEY_AUTH_TOKEN: String = "auth_token"

        /** Storage key for the cached user id of the active session. */
        const val KEY_USER_ID: String = "user_id"
    }
}
