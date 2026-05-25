package com.elysium.softwork.shared.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import org.junit.Rule
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
class SoftWorkCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSlotContent() {
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkCard {
                    Text(text = "Plan Pro")
                }
            }
        }

        composeRule.onNodeWithText("Plan Pro").assertIsDisplayed()
    }

    @Test
    fun composesArbitraryNestedLayoutsInsideTheSlot() {
        composeRule.setContent {
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

        composeRule.onNodeWithText("Header").assertIsDisplayed()
        composeRule.onNodeWithText("Body").assertIsDisplayed()
        composeRule.onNodeWithText("Footer").assertIsDisplayed()
    }
}
