package com.elysium.softwork.shared.application.usecase

import com.elysium.softwork.shared.data.local.SharedPrefsManager

/**
 * Reads the worker's forum-anonymity flag.
 *
 * Consumed by the forum composer and thread flows to decide whether outgoing content
 * carries the worker's identity. The flag is owned by the protected-identity screen;
 * this use case is read-only.
 *
 * Stateless; safe to share a single instance process-wide.
 *
 * @param prefs persistent key-value store backing the flag.
 */
class GetForumAnonymityUseCase(private val prefs: SharedPrefsManager) {

    /** @return `true` when forum activity must hide the worker's identity. */
    operator fun invoke(): Boolean = prefs.getBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY)
}
