package com.elysium.softwork.testsupport

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Verifies that `collectAsStateWithLifecycle()` honours the lifecycle gate.
 *
 * Why this test exists: the project rule mandates that every screen-level flow consumer
 * uses `collectAsStateWithLifecycle()` rather than `collectAsState()`. The lifecycle
 * variant pauses collection when the host moves below the minimum active state
 * (`Lifecycle.State.STARTED` by default). The pause is the property that keeps a
 * backgrounded SoftWork session from continuing to burn CPU on a 2-3 GB RAM device.
 *
 * The test drives a [TestLifecycleOwner] across the `STARTED` ↔ `CREATED` boundary
 * while emitting integer ticks into a [MutableStateFlow]. The composition renders the
 * latest tick as plain text; the test asserts that the rendered value freezes while
 * the lifecycle is at `CREATED` and resumes when it returns to `STARTED`.
 *
 * The lifecycle owner is injected via [LocalLifecycleOwner] so the composition under
 * test ignores the host Activity's real lifecycle — the host stays at RESUMED for the
 * full duration, only the lifecycle owner visible to the composable changes state.
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
class LifecycleAwareCollectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun collector_freezes_when_owner_drops_below_started() {
        val source = MutableStateFlow(0)
        val owner = TestLifecycleOwner(initialState = Lifecycle.State.STARTED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val current: Int by source.collectAsStateWithLifecycle()
                Text(text = "tick=$current")
            }
        }

        // STARTED → composition observes the initial value.
        composeRule.onNodeWithText("tick=0").assertExists()

        // STARTED → composition observes a fresh emission.
        source.value = 1
        composeRule.waitForIdle()
        composeRule.onNodeWithText("tick=1").assertExists()

        // Drop the owner below STARTED. Subsequent emissions must NOT reach the composition.
        composeRule.runOnUiThread { owner.currentState = Lifecycle.State.CREATED }
        composeRule.waitForIdle()

        source.value = 2
        source.value = 3
        composeRule.waitForIdle()

        // The composition is still showing the value captured at the boundary.
        composeRule.onNodeWithText("tick=1").assertExists()

        // Returning to STARTED resumes the collector; the latest emission propagates.
        composeRule.runOnUiThread { owner.currentState = Lifecycle.State.STARTED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("tick=3").assertExists()
    }
}
