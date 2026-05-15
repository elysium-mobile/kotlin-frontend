package com.elysium.softwork.shared.domain.identity

/**
 * Snapshot of the user's identity-protection settings. The four flags are independent —
 * the global switch acts as a master signal but does not force the granular flags on/off,
 * so the UI can keep "global ON, forum OFF" combinations if the user explicitly chose them.
 *
 * @property global master anonymity switch shown at the top of the protected-identity screen.
 * @property forum hide identity inside the workers' forum.
 * @property surveys hide identity in survey responses.
 * @property reports hide identity in incident reports.
 */
data class AnonymityPreferences(
    val global: Boolean = false,
    val forum: Boolean = false,
    val surveys: Boolean = false,
    val reports: Boolean = false,
)
