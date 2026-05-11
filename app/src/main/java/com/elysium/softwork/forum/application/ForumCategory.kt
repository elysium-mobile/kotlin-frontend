package com.elysium.softwork.forum.application

import androidx.annotation.StringRes
import com.elysium.softwork.R

/**
 * Wire keys for the four forum categories shipped in Phase 4. The [key] is what travels in
 * [com.elysium.softwork.forum.domain.model.Post.category] and gets persisted to Room; the
 * [labelRes] is the localized chip label rendered in the UI.
 *
 * Adding a new category means adding an entry here AND providing two new strings (en/es).
 * The default stays open (no `default()`), since the team will likely add categories without
 * a single canonical fallback.
 */
enum class ForumCategory(val key: String, @param:StringRes val labelRes: Int) {
    SUGGESTIONS("suggestions", R.string.forum_category_suggestions),
    QUESTIONS("questions", R.string.forum_category_questions),
    EVENTS("events", R.string.forum_category_events),
    CONFLICTS("conflicts", R.string.forum_category_conflicts);

    companion object {
        /** Lookup helper used to resolve a stored [key] back to an enum entry. */
        fun fromKey(key: String?): ForumCategory? = entries.firstOrNull { it.key == key }
    }
}
