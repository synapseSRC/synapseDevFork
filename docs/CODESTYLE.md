# 🎨 Code Style & Standards

To ensure a consistent codebase, we follow strict styling guidelines.

## 📝 General Kotlin
- **Naming:**
    - Classes: `PascalCase`
    - Functions/Variables: `camelCase`
    - Constants: `UPPER_SNAKE_CASE`
- **Formatting:**
    - Use 4 spaces for indentation.
    - Max line length: 120 characters.
    - No trailing whitespace.
- **Null Safety:**
    - Avoid `!!` (double-bang) operators. Use `?`, `?.`, `?:`, or `requireNotNull`.
    - Explicitly type public API returns.

## 🖼️ Jetpack Compose (UI)
- **Functions:** Must be `PascalCase` and Nouns (e.g., `PostCard`, not `drawPost`).
- **Modifiers:**
    - Every Composable must accept a `modifier: Modifier = Modifier` as the first optional parameter.
    - Apply the modifier to the root layout of the component.
- **State:**
    - Hoist state to the parent or ViewModel.
    - Use `remember` for ephemeral UI state (animations, scroll).
    - **Prohibited:** Business logic inside Composables.

## 🏗️ Project Structure
- **Imports:**
    - No wildcard imports (`import java.util.*`).
    - Order: Android/Google, Third-party, Project-specific.
- **File Layout:**
    1.  Package
    2.  Imports
    3.  Constants
    4.  Class/Object
    5.  Extension Functions (if relevant to the file)

## 🏛️ Architecture Conventions
- **ViewModels:**
    - Expose state as `StateFlow<UiState>`.
    - Expose events as functions (e.g., `fun onLoginClick()`).
    - Do not expose mutable state types (`MutableStateFlow`) publicly.
- **Repositories:**
    - Return `Result<T>` or `Flow<T>`.
    - Handle exceptions and map them to domain errors.

## 🧹 Linting
Run the linter to verify your code:
```bash
./gradlew lint
./gradlew ktlintCheck
```
