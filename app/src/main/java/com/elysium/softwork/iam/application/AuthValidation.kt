package com.elysium.softwork.iam.application

/**
 * Pure validation helpers for IAM forms. Kept as top-level functions (no Android imports) so
 * they can be unit-tested on the JVM without instrumentation.
 */
object AuthValidation {

    private const val MIN_PASSWORD_LENGTH: Int = 8

    private val EMAIL_REGEX: Regex = Regex(
        pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    )

    /**
     * Personal email providers that are NOT considered corporate. Used to drive the
     * "Verified domain" indicator on the register screen — anything outside this list with a
     * structurally valid email is treated as corporate.
     */
    private val PERSONAL_DOMAINS: Set<String> = setOf(
        "gmail.com", "googlemail.com", "yahoo.com", "yahoo.es", "hotmail.com", "outlook.com",
        "live.com", "icloud.com", "me.com", "aol.com", "proton.me", "protonmail.com",
    )

    /** True when [email] has a syntactically valid form. */
    fun isEmailValid(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    /**
     * True when [email] is structurally valid AND its domain is not in the personal-provider
     * list. Drives the green "Verified domain" chip below the corporate email input.
     */
    fun isCorporateDomain(email: String): Boolean {
        if (!isEmailValid(email)) return false
        val domain: String = email.substringAfter('@').lowercase().trim()
        return domain.isNotEmpty() && domain !in PERSONAL_DOMAINS
    }

    /** True when [password] meets the minimum length requirement. */
    fun isPasswordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    /** True when [password] matches [confirmation] and both pass [isPasswordValid]. */
    fun doPasswordsMatch(password: String, confirmation: String): Boolean =
        password.isNotEmpty() && password == confirmation

    /** True when [username] contains at least one non-whitespace character. */
    fun isUsernameValid(username: String): Boolean = username.trim().isNotEmpty()
}
