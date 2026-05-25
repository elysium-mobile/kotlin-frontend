package com.elysium.softwork.shared.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Test

/**
 * Instrumented Compose UI tests for [SoftWorkCard].
 *
 * The card is a content-agnostic container — the tests only verify that its slot
 * lambda renders, that nested composables propagate into the semantics tree, and
 * that arbitrary content is laid out inside.
 *
 * Visual properties (the 16.dp corner radius, the white surface, the 4.dp shadow,
 * the 1.dp AccentWhite border) are intentionally out of scope here. Pixel-level
 * regressions belong to screenshot tests and are tracked separately to keep the
 * functional suite under the 500 ms-per-test budget.
 */
@OptIn(ExperimentalTestApi::class)
class SoftWorkCardTest {

    @Test
    fun rendersSlotContent() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                SoftWorkCard {
                    Text(text = "Plan Pro")
                }
            }
        }

        onNodeWithText("Plan Pro").assertIsDisplayed()
    }

    @Test
    fun composesArbitraryNestedLayoutsInsideTheSlot() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                SoftWorkCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Header")
                        Text(text = "Body")
                        Text(text = "Footer")
                    }
                }
            }
        }

        onNodeWithText("Header").assertIsDisplayed()
        onNodeWithText("Body").assertIsDisplayed()
        onNodeWithText("Footer").assertIsDisplayed()
    }
}
