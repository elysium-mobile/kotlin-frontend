package com.elysium.softwork.shared.presentation.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.elysium.softwork.shared.presentation.theme.SoftWorkTheme
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Baseline Compose component test for [SoftWorkButton].
 *
 * Serves three purposes:
 *  1. **Smoke test for the brand button** — every screen depends on it; regressions in
 *     the click action, label rendering, or disabled state would cascade.
 *  2. **Template for future component tests.** The class establishes the conventions
 *     other component tests should follow: the [runComposeUiTest] builder, content
 *     wrapped in `SoftWorkTheme`, text-driven semantics queries, no `Thread.sleep`.
 *  3. **Verification that the disabled state is honored** — `SoftWorkButton` paints a
 *     0.5 alpha and gates the `clickable` modifier on the `enabled` flag, so a test that
 *     taps a disabled button must observe zero callback invocations.
 *
 * The test runs as instrumentation because Compose UI tests require the real Android
 * Choreographer and Skia renderer. Each test method is a v2 [runComposeUiTest] block —
 * the modern replacement for the now-deprecated `createComposeRule()` JUnit4 rule and
 * the original `runComposeUiTest`. The v2 builder scopes the composition lifecycle to
 * the test method and aligns with standard coroutine behavior: dispatched tasks queue
 * rather than execute immediately, so any test that asserts after a state mutation
 * should rely on the implicit synchronization performed by `performClick` and the
 * matchers, or call `waitForIdle()` explicitly.
 */
@OptIn(ExperimentalTestApi::class)
class SoftWorkButtonTest {


    @Test
    fun rendersLabelAndExposesClickAction() = runComposeUiTest {
        setContent {
            SoftWorkTheme {
                SoftWorkButton(text = "Pay membership", onClick = {})
            }
        }

        onNodeWithText("Pay membership")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun invokesCallbackOnTapWhenEnabled() = runComposeUiTest {
        var clicks = 0
        setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Add card",
                    onClick = { clicks += 1 },
                )
            }
        }

        onNodeWithText("Add card").performClick()
        onNodeWithText("Add card").performClick()

        assertEquals(2, clicks)
    }

    @Test
    fun swallowsTapsWhenDisabled() = runComposeUiTest {
        var clicked = false
        setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Add card",
                    onClick = { clicked = true },
                    enabled = false,
                )
            }
        }

        onNodeWithText("Add card").performClick()

        assertFalse(clicked)
    }

    @Test
    fun rendersUnderTheHRVariantWithoutCrashing() = runComposeUiTest {
        // The HR variant uses a solid PrimaryNavy fill instead of the EMPLOYEE gradient.
        // The test only smokes the rendering path — visual diffing is out of scope here.
        var rendered = false
        setContent {
            SoftWorkTheme {
                SoftWorkButton(
                    text = "Acknowledge",
                    onClick = {},
                    variant = ButtonVariant.HR,
                )
            }
            rendered = true
        }

        onNodeWithText("Acknowledge").assertIsDisplayed()
        assertTrue(rendered)
    }
}
