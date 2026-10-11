# Contributing to Focus

Thank you for your interest in contributing to Focus! We welcome community contributions, bug fixes, feature proposals, and documentation improvements.

---

## Code of Conduct

Please treat all community members with respect, empathy, and professional courtesy.

---

## How to Contribute

### 1. Reporting Bugs
- Search existing issues to avoid creating duplicates.
- Use the **Bug Report** template when opening a new issue.
- Include clear steps to reproduce, expected vs. actual behavior, and device/Android OS specs.

### 2. Suggesting Features
- Use the **Feature Request** template.
- Explain the user problem or workflow improvement clearly.

### 3. Submitting Pull Requests (PRs)
1. Fork the repository and create a feature branch off `main`:
   ```bash
   git checkout -b feature/my-new-feature
   ```
2. Adhere to existing Kotlin coding standards and Jetpack Compose patterns.
3. Verify that your changes compile and pass all unit tests:
   ```bash
   ./gradlew assembleDebug testDebugUnitTest
   ```
4. Open a Pull Request referencing the issue number and fill out the PR checklist.

---

## Coding Guidelines

- **Architecture:** Unidirectional Data Flow (MVVM) with Kotlin `StateFlow` and Room `Flow` streams.
- **Formatting:** Clean Kotlin style, zero unused imports, and descriptive variable naming.
- **Commits:** Conventional Commits convention (`feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`).
