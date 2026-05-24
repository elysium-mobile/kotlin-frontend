package com.elysium.softwork.testsupport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit4 [org.junit.rules.TestRule] that swaps the process-wide `Dispatchers.Main` for a
 * controllable [TestDispatcher] during the test, then restores the real dispatcher when
 * the test finishes.
 *
 * Required for any test that touches a `ViewModel`. The production code dispatches every
 * `viewModelScope.launch` onto `Dispatchers.Main.immediate`; without this swap the JVM
 * runtime crashes with `IllegalStateException: Module with the Main dispatcher had failed
 * to initialize` because there is no Android `Looper` on the host machine.
 *
 * **Why [UnconfinedTestDispatcher] is the default.** Unconfined eagerly executes every
 * coroutine on the calling thread, so a `StateFlow.value` read immediately after a
 * `viewModel.submit()` call sees the post-launch snapshot without an explicit
 * `advanceUntilIdle()` step. This keeps simple cases readable. Tests that need to assert
 * intermediate states (e.g. `Loading` before `Success`) should construct the rule with a
 * [kotlinx.coroutines.test.StandardTestDispatcher] and call `advanceUntilIdle()` /
 * `runCurrent()` manually.
 *
 * Usage:
 * ```
 * class MyViewModelTest {
 *     @get:Rule val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test fun `submit transitions to success`() = runTest {
 *         val vm = MyViewModel(FakeStore())
 *         vm.submit()
 *         assertEquals(State.Success, vm.state.value)
 *     }
 * }
 * ```
 *
 * @param testDispatcher dispatcher installed as `Main`. Defaults to
 *   [UnconfinedTestDispatcher]; pass a [kotlinx.coroutines.test.StandardTestDispatcher]
 *   when fine-grained ordering control is required.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
