package com.elysium.softwork.shared.utils.values

import androidx.annotation.StringRes
import com.elysium.softwork.R

/**
 * Catalog of report irregularity types accepted by the backend.
 *
 * **Category — value-bearing enum.** Same pattern as [ForumCategory]: [key] is the
 * locale-independent wire format persisted in
 * `com.elysium.softwork.worker.forum.domain.model.ForumReport.type`; [labelRes] supplies the
 * localized label rendered in the UI.
 *
 * Adding a new type means adding an entry here AND providing two new strings (en/es).
 */
enum class ReportType(val key: String, @param:StringRes val labelRes: Int) {
    HARASSMENT("harassment", R.string.report_type_harassment),
    DISCRIMINATION("discrimination", R.string.report_type_discrimination),
    SECURITY("security", R.string.report_type_security),
    ETHICS("ethics", R.string.report_type_ethics),
    OTHER("other", R.string.report_type_other);

    companion object {
        /** Lookup helper used to resolve a persisted [key] back to an enum entry. */
        fun fromKey(key: String?): ReportType? = entries.firstOrNull { it.key == key }
    }
}
