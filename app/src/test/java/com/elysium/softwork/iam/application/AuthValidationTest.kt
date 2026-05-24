package com.elysium.softwork.iam.application

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM unit tests for [AuthValidation].
 *
 * The validator is intentionally Android-free, so the test class exercises it directly
 * without any rule, fake, or coroutine helper. Each [Test] focuses on a single
 * documented behavior and uses the most descriptive backtick-style name available so
 * test reports double as a behavior specification.
 *
 * Coverage targets:
 *  - Email regex acceptance / rejection across well-formed and malformed inputs.
 *  - Corporate-domain classification: rejection of every personal provider listed in
 *    `Domains.PERSONAL`, acceptance of representative custom domains.
 *  - Password length boundary at the 8-character minimum.
 *  - Confirmation-match invariant including the explicit empty-string rejection.
 *  - Username acceptance: any string with at least one non-whitespace character.
 */
class AuthValidationTest {

    // region Email
    @Test
    fun `isEmailValid accepts standard corporate address`() {
        assertTrue(AuthValidation.isEmailValid("worker@elysium.com"))
    }

    @Test
    fun `isEmailValid accepts plus-tagged and dot-separated locals`() {
        assertTrue(AuthValidation.isEmailValid("first.last+tag@elysium.com"))
    }

    @Test
    fun `isEmailValid trims surrounding whitespace before applying the pattern`() {
        assertTrue(AuthValidation.isEmailValid("  worker@elysium.com  "))
    }

    @Test
    fun `isEmailValid rejects blank input`() {
        assertFalse(AuthValidation.isEmailValid(""))
        assertFalse(AuthValidation.isEmailValid("   "))
    }

    @Test
    fun `isEmailValid rejects missing at sign`() {
        assertFalse(AuthValidation.isEmailValid("workerelysium.com"))
    }

    @Test
    fun `isEmailValid rejects missing top level domain`() {
        assertFalse(AuthValidation.isEmailValid("worker@elysium"))
    }

    @Test
    fun `isEmailValid rejects single-character TLD`() {
        assertFalse(AuthValidation.isEmailValid("worker@elysium.c"))
    }
    // endregion

    // region Corporate domain
    @Test
    fun `isCorporateDomain accepts a custom workplace domain`() {
        assertTrue(AuthValidation.isCorporateDomain("worker@elysium.com"))
    }

    @Test
    fun `isCorporateDomain is case-insensitive on the host part`() {
        assertTrue(AuthValidation.isCorporateDomain("Worker@ELYSIUM.com"))
    }

    @Test
    fun `isCorporateDomain rejects every personal provider in the deny list`() {
        val personalSamples: List<String> = listOf(
            "user@gmail.com",
            "user@googlemail.com",
            "user@yahoo.com",
            "user@yahoo.es",
            "user@hotmail.com",
            "user@outlook.com",
            "user@live.com",
            "user@icloud.com",
            "user@me.com",
            "user@aol.com",
            "user@proton.me",
            "user@protonmail.com",
        )
        personalSamples.forEach { sample ->
            assertFalse(
                "$sample should be classified as non-corporate",
                AuthValidation.isCorporateDomain(sample),
            )
        }
    }

    @Test
    fun `isCorporateDomain rejects structurally invalid input even when domain looks corporate`() {
        assertFalse(AuthValidation.isCorporateDomain("not-an-email"))
        assertFalse(AuthValidation.isCorporateDomain("@elysium.com"))
    }
    // endregion

    // region Password
    @Test
    fun `isPasswordValid rejects a 7-character password`() {
        assertFalse(AuthValidation.isPasswordValid("Abc123!"))
    }

    @Test
    fun `isPasswordValid accepts the exact 8-character minimum`() {
        assertTrue(AuthValidation.isPasswordValid("Abc1234!"))
    }

    @Test
    fun `isPasswordValid accepts long passwords`() {
        assertTrue(AuthValidation.isPasswordValid("a_very_long_passphrase_that_definitely_passes"))
    }

    @Test
    fun `doPasswordsMatch returns true for equal non-empty values`() {
        assertTrue(AuthValidation.doPasswordsMatch("Abc1234!", "Abc1234!"))
    }

    @Test
    fun `doPasswordsMatch returns false when the values differ`() {
        assertFalse(AuthValidation.doPasswordsMatch("Abc1234!", "Abc1234?"))
    }

    @Test
    fun `doPasswordsMatch returns false when both values are empty`() {
        assertFalse(AuthValidation.doPasswordsMatch("", ""))
    }
    // endregion

    // region Username
    @Test
    fun `isUsernameValid accepts trimmed non-empty values`() {
        assertTrue(AuthValidation.isUsernameValid("Cesar"))
        assertTrue(AuthValidation.isUsernameValid("  Cesar  "))
    }

    @Test
    fun `isUsernameValid rejects empty and whitespace-only values`() {
        assertFalse(AuthValidation.isUsernameValid(""))
        assertFalse(AuthValidation.isUsernameValid("   "))
        assertFalse(AuthValidation.isUsernameValid("\t\n"))
    }
    // endregion
}
