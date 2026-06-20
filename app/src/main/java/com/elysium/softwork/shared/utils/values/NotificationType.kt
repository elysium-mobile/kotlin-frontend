package com.elysium.softwork.shared.utils.values

/**
 * Discriminator for an in-app notification.
 *
 * Carries a stable wire [key] so the backend can round-trip the value as a string without
 * leaking enum ordinals. Localized labels (and category colors) are resolved by the
 * Composable layer based on the constant — there is no `labelRes` here because the
 * notification list renders the per-item `title` / `description` straight from the
 * payload, not the category name.
 *
 * Categories drive the color theming on `NotificationsScreen`:
 * - [SURVEY] → AccentMint / PrimaryTeal
 * - [PAYMENT] → soft warning surface / Warning
 * - [FORUM] → soft sky surface / PrimarySky
 * - [MESSAGE] → soft navy surface / PrimaryNavy
 */
enum class NotificationType(val key: String) {
    SURVEY("survey"),
    PAYMENT("payment"),
    FORUM("forum"),
    MESSAGE("message");

    companion object {
        /**
         * Resolves a wire key into a [NotificationType], returning `null` on unknown input.
         *
         * Matching is **case-insensitive** so the backend may send the category in any casing
         * (`survey`, `SURVEY`, `Survey`) and still resolve precisely.
         */
        fun fromKey(key: String?): NotificationType? =
            key?.let { raw -> entries.firstOrNull { it.key.equals(raw, ignoreCase = true) } }
    }
}
