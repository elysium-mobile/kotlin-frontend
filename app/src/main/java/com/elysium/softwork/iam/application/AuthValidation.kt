package com.elysium.softwork.iam.application

import com.elysium.softwork.shared.utils.constants.Domains
import com.elysium.softwork.shared.utils.constants.Regexes

/**
 * Pure validation helpers for IAM forms. Kept as top-level functions (no Android imports) so
 * they can be unit-tested on the JVM without instrumentation.
 *
 * Regex patterns and domain allow/deny lists are pulled from [Regexes] / [Domains] in
 * `shared/utils/constants/` rather than defined inline, so other validators (e.g. a future
 * incident-report email field) can reuse the same canonical sources.
 */
object AuthValidation {

    private const val MIN_PASSWORD_LENGTH: Int = 8

    /** True when [email] has a syntactically valid form. */
    fun isEmailValid(email: String): Boolean = Regexes.EMAIL.matches(email.trim())

    /**
     * True when [email] is structurally valid AND its domain is not in the personal-provider
     * list. Drives the green "Verified domain" chip below the corporate email input.
     */
    fun isCorporateDomain(email: String): Boolean {
        if (!isEmailValid(email)) return false
        val domain: String = email.substringAfter('@').lowercase().trim()
        return domain.isNotEmpty() && domain !in Domains.PERSONAL
    }

    /** True when [password] meets the minimum length requirement. */
    fun isPasswordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** True when [password] matches [confirmation] and both pass [isPasswordValid]. */
    fun doPasswordsMatch(password: String, confirmation: String): Boolean =
        password.isNotEmpty() && password == confirmation

    /** True when [username] contains at least one non-whitespace character. */
    fun isUsernameValid(username: String): Boolean = username.trim().isNotEmpty()
}
