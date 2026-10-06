package com.example.focus

import android.app.Application
import com.example.focus.model.TimerMode
import com.example.focus.viewmodel.FocusTimerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FocusTimerViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var viewModel: FocusTimerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FocusTimerViewModel(Application())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals(TimerMode.FOCUS, viewModel.timerMode.value)
        assertEquals(1500, viewModel.totalTimeSeconds.value)
        assertEquals(1500, viewModel.remainingTimeSeconds.value)
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun setMode_updatesTimerModeAndDurations() {
        viewModel.setMode(TimerMode.SHORT_BREAK)
        assertEquals(TimerMode.SHORT_BREAK, viewModel.timerMode.value)
        assertEquals(300, viewModel.totalTimeSeconds.value)
        assertEquals(300, viewModel.remainingTimeSeconds.value)
        assertFalse(viewModel.isRunning.value)

        viewModel.setMode(TimerMode.LONG_BREAK)
        assertEquals(TimerMode.LONG_BREAK, viewModel.timerMode.value)
        assertEquals(900, viewModel.totalTimeSeconds.value)
        assertEquals(900, viewModel.remainingTimeSeconds.value)
    }

    @Test
    fun playAndPause_togglesIsRunning() {
        viewModel.play()
        assertTrue(viewModel.isRunning.value)

        viewModel.pause()
        assertFalse(viewModel.isRunning.value)

        viewModel.togglePlayPause()
        assertTrue(viewModel.isRunning.value)

        viewModel.togglePlayPause()
        assertFalse(viewModel.isRunning.value)
    }

    @Test
    fun countdown_decrementsRemainingTime() = runTest(testScheduler) {
        viewModel.play()
        assertTrue(viewModel.isRunning.value)

        advanceTimeBy(3005L)
        assertEquals(1497, viewModel.remainingTimeSeconds.value)

        viewModel.pause()
        advanceTimeBy(3000L)
        assertEquals(1497, viewModel.remainingTimeSeconds.value)
    }

    @Test
    fun reset_resetsRemainingTimeToModeDuration() = runTest(testScheduler) {
        viewModel.play()
        advanceTimeBy(5005L)
        assertEquals(1495, viewModel.remainingTimeSeconds.value)

        viewModel.reset()
        assertFalse(viewModel.isRunning.value)
        assertEquals(1500, viewModel.remainingTimeSeconds.value)
    }
}
