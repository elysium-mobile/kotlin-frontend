package com.elysium.softwork.shared.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI tests for [InitialsAvatar].
 *
 * Verifies the static initials derivation. The avatar takes a `fullName` string and
 * surfaces up to two uppercase glyphs:
 *  - Two-word names → first letter of each part.
 *  - Single-word names → first letter only.
 *  - Lowercase input → uppercased glyphs.
 *  - Multi-whitespace input → collapsed before splitting.
 *
 * Visual properties (the 40.dp default diameter, the PrimarySky background, the
 * `titleMedium` weight) belong to screenshot tests and are intentionally out of
 * scope here.
 *
 * The class uses the JUnit4 v2 [createComposeRule] entry point
 * (`androidx.compose.ui.test.junit4.v2.createComposeRule`). The v2 namespace keeps
 * the class-scoped `composeRule` property that the original JUnit4 rule provided,
 * while routing through the same v2 internals as the `runComposeUiTest { ... }`
 * builder. This is the supported entry point because the legacy
 * `androidx.compose.ui.test.junit4.createComposeRule` is deprecated, and the
 * top-level `runComposeUiTest { ... }` builder is also deprecated in favor of the
 * v2 namespace. Module-level dependency pinning of `kotlinx-coroutines-android`
 * keeps the runtime classpath aligned with the version `kotlinx-coroutines-test`
 * was compiled against, so the v2 path resolves cleanly on-device.
 */
class InitialsAvatarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersTwoUppercaseGlyphsForATwoWordName() {
        composeRule.setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "Cesar Cardholder")
            }
        }

        composeRule.onNodeWithText("CC").assertIsDisplayed()
    }

    @Test
    fun rendersASingleGlyphForAOneWordName() {
        composeRule.setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "Cesar")
            }
        }

        composeRule.onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun uppercasesLowercaseInput() {
        composeRule.setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "alice ng")
            }
        }

        composeRule.onNodeWithText("AN").assertIsDisplayed()
    }

    @Test
    fun collapsesMultipleWhitespaceBeforeDerivingInitials() {
        composeRule.setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "  Maria   Lopez  ")
            }
        }

        composeRule.onNodeWithText("ML").assertIsDisplayed()
    }
}
