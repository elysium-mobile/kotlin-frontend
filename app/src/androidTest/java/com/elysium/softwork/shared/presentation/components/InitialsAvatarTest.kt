package com.elysium.softwork.shared.presentation.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Test

/**
 * Instrumented Compose UI tests for [InitialsAvatar].
 *
 * Verifies the static initials derivation. The avatar takes a `fullName` string and
 * surfaces up to two uppercase glyphs:
 *  - Two-word names → first letter of each part.
 *  - Single-word names → first letter only.
 *  - Lowercase input → uppercased glyphs.
 *  - Blank input → empty text (no crash).
 *  - Multi-whitespace input → collapsed before splitting.
 *
 * Visual properties (the 40.dp default diameter, the PrimarySky background, the
 * `titleMedium` weight) belong to screenshot tests and are intentionally out of
 * scope here.
 */
@OptIn(ExperimentalTestApi::class)
class InitialsAvatarTest {

    @Test
    fun rendersTwoUppercaseGlyphsForATwoWordName() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "Cesar Cardholder")
            }
        }

        onNodeWithText("CC").assertIsDisplayed()
    }

    @Test
    fun rendersASingleGlyphForAOneWordName() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "Cesar")
            }
        }

        onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun uppercasesLowercaseInput() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "alice ng")
            }
        }

        onNodeWithText("AN").assertIsDisplayed()
    }

    @Test
    fun collapsesMultipleWhitespaceBeforeDerivingInitials() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                InitialsAvatar(fullName = "  Maria   Lopez  ")
            }
        }

        onNodeWithText("ML").assertIsDisplayed()
    }
}
