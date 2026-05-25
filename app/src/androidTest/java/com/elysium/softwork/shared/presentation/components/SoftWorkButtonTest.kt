package com.elysium.softwork.shared.presentation.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Compose component test for [SoftWorkButton].
 *
 * Serves three purposes:
 *  1. **Smoke test for the brand button** — every screen depends on it; regressions in
 *     the click action, label rendering, or disabled state would cascade.
 *  2. **Template for future component tests.** The class establishes the conventions
 *     other component tests should follow: a single class-scoped `composeRule`,
 *     content wrapped in `SoftWorkTheme`, text-driven semantics queries, no
 *     `Thread.sleep`.
 *  3. **Verification that the disabled state is honored** — `SoftWorkButton` paints a
 *     0.5 alpha and gates the `clickable` modifier on the `enabled` flag, so a test that
 *     taps a disabled button must observe zero callback invocations.
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
class SoftWorkButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersLabelAndExposesClickAction() {
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkButton(text = "Pay membership", onClick = {})
            }
        }

        composeRule.onNodeWithText("Pay membership")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun invokesCallbackOnTapWhenEnabled() {
        var clicks = 0
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Add card",
                    onClick = { clicks += 1 },
                )
            }
        }

        composeRule.onNodeWithText("Add card").performClick()
        composeRule.onNodeWithText("Add card").performClick()

        assertEquals(2, clicks)
    }

    @Test
    fun swallowsTapsWhenDisabled() {
        var clicked = false
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Add card",
                    onClick = { clicked = true },
                    enabled = false,
                )
            }
        }

        composeRule.onNodeWithText("Add card").performClick()

        assertFalse(clicked)
    }

    @Test
    fun rendersUnderTheHRVariantWithoutCrashing() {
        var rendered = false
        composeRule.setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Acknowledge",
                    onClick = {},
                    variant = ButtonVariant.HR,
                )
            }
            rendered = true
        }

        composeRule.onNodeWithText("Acknowledge").assertIsDisplayed()
        assertTrue(rendered)
    }
}
