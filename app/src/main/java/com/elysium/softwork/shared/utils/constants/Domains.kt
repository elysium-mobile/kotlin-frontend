package com.elysium.softwork.shared.utils.constants

/**
 * Process-wide catalog of domain-name allow/deny lists. Any code that classifies an email
 * domain — verified-corporate chip, anti-spam guards, future SSO routing — should consume
 * the sets here rather than re-defining its own list.
 *
 * All entries are lowercase ASCII; callers must `lowercase()` the domain they're checking
 * before doing a lookup.
 */
object Domains {

    /**
     * Personal email providers that are NOT considered corporate. Drives the
     * "Verified domain" indicator on the register screen — anything outside this set with
     * a structurally valid email is treated as corporate.
     *
     * Curated rather than exhaustive: covers the long tail of consumer providers our
     * employees might accidentally type when registering. Add new entries as they show up
     * in support tickets; never remove an entry without confirming nobody legitimately
     * uses that provider for work email.
     */
    val PERSONAL: Set<String> = setOf(
        "gmail.com", "googlemail.com",
        "yahoo.com", "yahoo.es",
        "hotmail.com", "outlook.com", "live.com",
        "icloud.com", "me.com",
        "aol.com",
        "proton.me", "protonmail.com",
    )
}
