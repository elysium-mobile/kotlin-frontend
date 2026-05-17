package com.elysium.softwork.shared.utils.constants

/**
 * Process-wide [Regex] catalog. Patterns that are reused across more than one validator
 * or that benefit from a single canonical definition live here — never inlined inside a
 * call site.
 *
 * Regex literals are expensive to compile, so each entry is declared as a `val` and reused
 * across calls. Add a KDoc line above every new entry explaining the shape it matches.
 */
object Regexes {

    /**
     * Loose RFC 5322 surrogate accepted by the IAM forms.
     * Matches `local-part@domain.tld` with:
     * - local-part: letters / digits / `._%+-`
     * - domain: letters / digits / `.-`
     * - TLD: 2+ letters
     *
     * Intentionally permissive — full RFC compliance is the server's job; the client
     * only blocks obvious typos before round-tripping to the backend.
     */
    val EMAIL: Regex = Regex(
        pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    )
}
