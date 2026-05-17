package com.elysium.softwork.shared.utils.values

/**
 * Languages explicitly supported by the SoftWork Employee client.
 *
 * **Category — value-bearing enum.** Each entry pairs the enum constant with a stable
 * BCP-47 [tag] consumed by `LocaleListCompat.forLanguageTags(...)`.
 *
 * - [SYSTEM] follows the device language and falls back to English when the active locale has
 *   no translation. Encoded as a `null` [tag] so the AppCompat / `LocaleManager` layer treats
 *   it as "clear the per-app override".
 * - [EN] / [ES] map to BCP-47 language tags consumed by `LocaleListCompat.forLanguageTags`.
 */
enum class AppLocale(val tag: String?) {
    SYSTEM(null),
    EN("en"),
    ES("es"),
}
