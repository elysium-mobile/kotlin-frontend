package com.elysium.softwork.shared.application.usecase

import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.domain.identity.AnonymityPreferences

/**
 * Loads the worker's anonymity preferences as a single immutable snapshot.
 *
 * Bundles the four persisted flags (global, forum, surveys, reports) into one
 * [AnonymityPreferences] value so consumers never observe a half-read state.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param prefs persistent key-value store backing the flags.
 */
class LoadAnonymityPreferencesUseCase(private val prefs: SharedPrefsManager) {

    /** @return the current persisted snapshot; unset flags default to `false`. */
    operator fun invoke(): AnonymityPreferences = AnonymityPreferences(
        global = prefs.getBoolean(SharedPrefsManager.KEY_GLOBAL_ANONYMITY),
        forum = prefs.getBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY),
        surveys = prefs.getBoolean(SharedPrefsManager.KEY_SURVEYS_ANONYMITY),
        reports = prefs.getBoolean(SharedPrefsManager.KEY_REPORTS_ANONYMITY),
    )
}
