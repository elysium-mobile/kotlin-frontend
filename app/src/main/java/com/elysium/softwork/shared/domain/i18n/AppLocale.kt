package com.elysium.softwork.shared.domain.i18n

/**
 * Languages explicitly supported by the SoftWork Employee client.
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
