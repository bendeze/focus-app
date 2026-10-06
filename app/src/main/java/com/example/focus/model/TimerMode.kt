package com.example.focus.model

enum class TimerMode(val durationInSeconds: Int, val label: String) {
    FOCUS(25 * 60, "Focus"),
    SHORT_BREAK(5 * 60, "Short Break"),
    LONG_BREAK(15 * 60, "Long Break")
}
