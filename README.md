# Focus — Minimalist Focus & Task Manager App

Focus is an offline-first Android application designed for deep productivity, pomodoro-style focus sessions, and structured project management. Built with Jetpack Compose, Kotlin Coroutines, Room Database, and Retrofit for cloud synchronization.

---

## Key Features

- **Minimalist Monochromatic UI:** High-contrast, OLED-friendly black-and-white theme designed for distraction-free focus.
- **Custom Timer Engine:** Pomodoro presets (Focus, Short Break, Long Break) plus an Apple-style wheel picker for custom minutes and seconds.
- **Task & Project Management:**
  - Dynamic local calculation of project completion metrics.
  - Project organization with standalone inbox tasks and child project tasks.
  - Real-time remaining focus time updates (`PAUSED • Rem: Xm Ys`).
- **Offline-First Synchronization:**
  - 0ms local mutation speed backed by Room SQLite DB.
  - Offline user authentication and credential caching (SHA-256).
  - Background 2-way delta sync (`SyncEngine`) with automatic conflict resolution and `X-Idempotency-Key` headers to prevent duplicates.
- **Audio & Haptic Experience:** Customizable 15s countdown sound styles (Beep, Tick, Mute) and timer end alarms with vibration feedback.
- **Do Not Disturb Interop:** System Do Not Disturb permission handling and screen pinning lock integration.

---

## Technical Architecture

- **UI Framework:** Jetpack Compose with Material 3.
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Reactive Unidirectional Data Flow (`StateFlow` & Room `Flow` streams).
- **Local Persistence:** Room Database (`ProjectDao`, `TaskDao`, `FocusSessionDao`).
- **Networking:** Retrofit 2 + Moshi + OkHttp.
- **Asynchrony:** Kotlin Coroutines & `Flow`.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK 35 (minSdkVersion 24, targetSdkVersion 35)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Focus.git
   cd Focus
   ```
2. Build the project and run unit tests:
   ```bash
   ./gradlew.bat assembleDebug testDebugUnitTest
   ```

---

## License

This project is licensed under the [MIT License](LICENSE).
