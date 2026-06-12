package com.elysium.softwork.shared.application.usecase

import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.domain.identity.AnonymityPreferences

/**
 * Persists a full anonymity-preferences snapshot.
 *
 * Writes all four flags so the persisted state always mirrors one coherent
 * [AnonymityPreferences] value — partial writes are not possible through this path.
 * Idempotent: saving the same snapshot twice is a no-op at the storage level.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param prefs persistent key-value store backing the flags.
 */
class SaveAnonymityPreferencesUseCase(private val prefs: SharedPrefsManager) {

    /** @param snapshot the complete preference set to persist. */
    operator fun invoke(snapshot: AnonymityPreferences) {
        prefs.putBoolean(SharedPrefsManager.KEY_GLOBAL_ANONYMITY, snapshot.global)
        prefs.putBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY, snapshot.forum)
        prefs.putBoolean(SharedPrefsManager.KEY_SURVEYS_ANONYMITY, snapshot.surveys)
        prefs.putBoolean(SharedPrefsManager.KEY_REPORTS_ANONYMITY, snapshot.reports)
    }
}
