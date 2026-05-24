package com.elysium.softwork.testsupport

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
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
 * The test uses the v2 [runComposeUiTest] builder rather than the now-deprecated
 * `createComposeRule()` JUnit4 rule or the original `runComposeUiTest`. The v2 block
 * exposes a `ComposeUiTest` receiver whose surface (`setContent`, `onNodeWithText`,
 * `waitForIdle`, `runOnUiThread`) is identical to the legacy API, but it aligns with
 * standard coroutine behavior by queuing dispatched tasks instead of executing them
 * eagerly. Every state mutation in this test is therefore paired with an explicit
 * `waitForIdle()` call so the recomposition catches up before the assertion runs.
 */
@OptIn(ExperimentalTestApi::class)
class LifecycleAwareCollectionTest {

    @Test
    fun collector_freezes_when_owner_drops_below_started() = runComposeUiTest {
        val source = MutableStateFlow(0)
        val owner = TestLifecycleOwner(initialState = Lifecycle.State.STARTED)

        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val current: Int by source.collectAsStateWithLifecycle()
                Text(text = "tick=$current")
            }
        }

        // STARTED → composition observes the initial value.
        onNodeWithText("tick=0").assertExists()

        // STARTED → composition observes a fresh emission.
        source.value = 1
        waitForIdle()
        onNodeWithText("tick=1").assertExists()

        // Drop the owner below STARTED. Subsequent emissions must NOT reach the composition.
        runOnUiThread { owner.currentState = Lifecycle.State.CREATED }
        waitForIdle()

        source.value = 2
        source.value = 3
        waitForIdle()

        // The composition is still showing the value captured at the boundary.
        onNodeWithText("tick=1").assertExists()

        // Returning to STARTED resumes the collector; the latest emission propagates.
        runOnUiThread { owner.currentState = Lifecycle.State.STARTED }
        waitForIdle()

        onNodeWithText("tick=3").assertExists()
    }
}
