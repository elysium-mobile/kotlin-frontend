package com.elysium.softwork.shared.utils.values

import androidx.annotation.StringRes
import com.elysium.softwork.R

/**
 * Catalog of company areas / departments selectable when filing a forum report.
 *
 * **Category — value-bearing enum.** [key] is the locale-independent wire format persisted
 * in `com.elysium.softwork.worker.forum.domain.model.ForumReport.area`; [labelRes] supplies
 * the localized label rendered in the area dropdown.
 *
 * Adding a new area means adding an entry here AND providing two new strings (en/es).
 */
enum class ReportArea(val key: String, @param:StringRes val labelRes: Int) {
    TECHNOLOGY("technology", R.string.report_area_technology),
    HUMAN_RESOURCES("human_resources", R.string.report_area_human_resources),
    OPERATIONS("operations", R.string.report_area_operations),
    SALES("sales", R.string.report_area_sales),
    MARKETING("marketing", R.string.report_area_marketing),
    ADMINISTRATION("administration", R.string.report_area_administration);

    companion object {
        /** Lookup helper used to resolve a persisted [key] back to an enum entry. */
        fun fromKey(key: String?): ReportArea? = entries.firstOrNull { it.key == key }
    }
}
