package com.elysium.softwork.shared.utils.values

/**
 * Lifecycle status the server may assign to a `ForumReport`.
 *
 * **Category — value-bearing enum.** Each entry carries a stable wire [key] that the
 * backend uses in the `status` field of the report payload.
 *
 * - [PENDING] — the report was received but no reviewer has picked it up yet.
 * - [UNDER_REVIEW] — an HR reviewer is actively working on it.
 * - [RESOLVED] — the case has been closed with an outcome communicated to the reporter.
 * - [DISMISSED] — the report was reviewed and deemed not actionable.
 */
enum class ReportStatus(val key: String) {
    PENDING("pending"),
    UNDER_REVIEW("under_review"),
    RESOLVED("resolved"),
    DISMISSED("dismissed");

    companion object {
        /** Resolves a server [key] back to an enum entry; falls back to [PENDING]. */
        fun fromKey(key: String?): ReportStatus = entries.firstOrNull { it.key == key } ?: PENDING
    }
}
