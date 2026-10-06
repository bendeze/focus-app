# Project Plan

Create a "Pomodoro"-style Focus Timer application using Jetpack Compose. Modern UI using Material 3 guidelines, light-red ("tomato") color motif. Features include: Segmented Button row to toggle between Focus (25 min), Short Break (5 min), and Long Break (15 min); Large countdown timer (MM:SS) in center surrounded by circular progress bar; Large Play/Pause button and smaller Reset button; Snackbar notification and default system sound when timer hits 00:00.

## Project Brief

# Project Brief: Focus Timer App

## Features
1. **Mode Selection**: Toggle between Focus (25 min), Short Break (5 min), and Long Break (15 min) timer presets using a Segmented Button row.
2. **Visual Countdown Timer**: Center-aligned countdown display (MM:SS format) encircled by a dynamic circular progress indicator.
3. **Playback Controls**: Prominent Play/Pause button to control timer execution alongside a Reset button to revert to the active mode's duration.
4. **Completion Notifications**: Audio alert using system default sound and an onscreen Snackbar notification upon timer reaching 00:00.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 design system (custom light-red / "tomato" color motif)
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and Compose Material Adaptive library for screen layouts
- **State & Concurrency**: Kotlin Coroutines, Flow / StateFlow, and Jetpack ViewModel
- **System Integrations**: Android MediaPlayer / AudioTrack for audio playback and Compose SnackbarHost for notifications

## Implementation Steps
**Total Duration:** 18m 1s

### Task_1_SetupThemeAndViewModel: Define Material 3 light-red tomato color theme and create ViewModel with state management for timer modes (Focus 25min, Short Break 5min, Long Break 15min), countdown logic, play/pause/reset controls, and completion signals.
- **Status:** COMPLETED
- **Updates:** Completed Material 3 light-red tomato theme setup in Theme.kt and Color.kt. Implemented TimerMode enum (Focus 25m, Short Break 5m, Long Break 15m) and FocusTimerViewModel with StateFlows for mode, remainingTime, totalTime, isRunning, and completion SharedFlow with system sound playback. All unit tests passed and debug build succeeded.
- **Acceptance Criteria:**
  - Material 3 light-red tomato theme configured
  - FocusTimerViewModel handles timer countdown with Coroutines and StateFlow
  - Supports 3 presets: Focus (25 min), Short Break (5 min), Long Break (15 min)
  - Project builds successfully
- **Duration:** 14m 16s

### Task_2_ImplementFocusTimerUI: Implement the Focus Timer main screen using Jetpack Compose with Segmented Button mode selector, circular progress timer display (MM:SS), action controls (Play/Pause, Reset), and completion alert handling (MediaPlayer sound and Snackbar notification).
- **Status:** COMPLETED
- **Updates:** Implemented FocusTimerScreen in Jetpack Compose using Material 3 SingleChoiceSegmentedButtonRow for mode selection, circular progress indicator with centered MM:SS display, LargeFloatingActionButton for Play/Pause, OutlinedButton for Reset, and SnackbarHost with system sound trigger on completion events. Integrated into MainActivity and added Compose @Previews. Debug assemble and unit tests passed.
- **Acceptance Criteria:**
  - Segmented Button row allows seamless mode switching
  - Circular progress bar updates smoothly with timer display
  - Play/Pause and Reset buttons function correctly
  - Audio alert and Snackbar trigger upon countdown completion
  - Build passes without errors
- **Duration:** 3m 45s

### Task_3_RunAndVerify: Run and verify application stability, test all timer operations (play, pause, reset, mode change, countdown completion notification), and confirm alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified application implementation, Material 3 tomato theme, timer state management, unit tests logic, and UI layout. All features conform to the brief requirements.
- **Acceptance Criteria:**
  - Build pass
  - App does not crash
  - Make sure all existing tests pass
  - All brief features verified and working properly

