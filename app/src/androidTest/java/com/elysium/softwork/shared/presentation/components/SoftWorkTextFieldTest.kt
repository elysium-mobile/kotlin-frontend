package com.elysium.softwork.shared.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Compose UI tests for [SoftWorkTextField].
 *
 * The brand text field is the foundation of every authentication and payment form.
 * Behaviors under test:
 *  - The placeholder text is rendered before the worker types.
 *  - Typing forwards each character to the hoisted `onValueChange` callback so the
 *    parent state holder owns the truth.
 *  - Clearing the field empties the hoisted value back to an empty string.
 *
 * The class uses the JUnit4 v2 [createComposeRule] entry point
 * (`androidx.compose.ui.test.junit4.v2.createComposeRule`). The v2 namespace keeps
 * the class-scoped `@get:Rule` ergonomics that the original JUnit4 rule provided,
 * while routing through the same v2 internals as the `runComposeUiTest { ... }`
 * builder. This is the supported entry point because the legacy
 * `androidx.compose.ui.test.junit4.createComposeRule` is deprecated, and the
 * top-level `runComposeUiTest { ... }` builder is also deprecated in favor of the
 * v2 namespace. Module-level dependency pinning of `kotlinx-coroutines-android`
 * keeps the runtime classpath aligned with the version `kotlinx-coroutines-test`
 * was compiled against, so the v2 path resolves cleanly on-device.
 */
class SoftWorkTextFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersPlaceholderBeforeInput() {
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Corporate email",
                )
            }
        }

        composeRule.onNodeWithText("Corporate email").assertIsDisplayed()
    }

    @Test
    fun emitsValueChangeOnTextInput() {
        var captured = ""
        composeRule.setContent {
            var hoisted by remember { mutableStateOf("") }
            SoftWorkTheme {
                SoftWorkTextField(
                    value = hoisted,
                    onValueChange = { typed ->
                        hoisted = typed
                        captured = typed
                    },
                    placeholder = "Card number",
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("4242")

        // The semantics tree reflects the current hoisted value, and the callback
        // received the latest typed string.
        composeRule.onNode(hasSetTextAction()).assertTextEquals("4242")
        assertEquals("4242", captured)
    }

    @Test
    fun clearingEmptiesTheHoistedValue() {
        var captured = "starting"
        composeRule.setContent {
            var hoisted by remember { mutableStateOf("starting") }
            SoftWorkTheme {
                SoftWorkTextField(
                    value = hoisted,
                    onValueChange = { typed ->
                        hoisted = typed
                        captured = typed
                    },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextClearance()

        assertEquals("", captured)
    }
}
