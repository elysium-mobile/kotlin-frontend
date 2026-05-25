package com.elysium.softwork.shared.presentation.components

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Assert.assertEquals
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
 * Each test creates a scoped `runComposeUiTest` block (v2) and renders the field
 * under [SoftWorkTheme] so the color and border treatments match production.
 */
@OptIn(ExperimentalTestApi::class)
class SoftWorkTextFieldTest {

    @Test
    fun rendersPlaceholderBeforeInput() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                SoftWorkTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Corporate email",
                )
            }
        }

        onNodeWithText("Corporate email").assertIsDisplayed()
    }

    @SuppressLint("UnrememberedMutableState")
    @Test
    fun emitsValueChangeOnTextInput() = runComposeUiTest {
        var captured = ""
        setContent {
            var hoisted by mutableStateOf("")
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

        onNode(hasSetTextAction()).performTextInput("4242")

        // The semantics tree reflects the current hoisted value, and the callback
        // received the latest typed string.
        onNode(hasSetTextAction()).assertTextEquals("4242")
        assertEquals("4242", captured)
    }

    @SuppressLint("UnrememberedMutableState")
    @Test
    fun clearingEmptiesTheHoistedValue() = runComposeUiTest {
        var captured = "starting"
        setContent {
            var hoisted by mutableStateOf("starting")
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

        onNode(hasSetTextAction()).performTextClearance()

        assertEquals("", captured)
    }
}
