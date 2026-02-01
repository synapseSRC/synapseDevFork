# 🤖 AI Agent Instructions & Rules

> **⚠️ CRITICAL FOR AI AGENTS:** This file represents the **absolute source of truth** for your behavior in this repository. Deviating from these rules will result in rejected code. Read this strictly.

## 🧠 Core Philosophy
You are working on a **production-grade Android application** that is actively migrating to a **Kotlin Multiplatform (KMP)** architecture.
- **Current State:** Hybrid (Native Android UI + Shared KMP Engine).
- **Goal:** Maximum code sharing in `shared`, native performance in `app`.
- **Quality:** "Flagship Open Source" — clean, documented, tested, and scalable.

---

## 🚫 STRICT PROHIBITIONS (Anti-Patterns)
**Do NOT do any of the following. Zero exceptions.**

### 1. ❌ Architecture Violations
- **NO Retrofit:** Use **Ktor** and **Supabase-kt** exclusively.
- **NO Room (in Android-only):** Database logic belongs in the `shared` module (using SQLDelight/Room KMP).
- **NO Business Logic in UI:** Composables must *only* render state and emit events.
- **NO Business Logic in ViewModels:** ViewModels are for state orchestration only. Delegate logic to UseCases/Interactors.
- **NO Platform Leaks:** Never import `android.*` or `java.*` packages in `commonMain`.

### 2. ❌ State Management Errors
- **NO Mutable State in Composables:** Never use `var` with `remember` for data that drives business logic. Use `StateFlow` from ViewModels.
- **NO "God ViewModels":** Do not create monolithic ViewModels. Scope them strictly to the feature/screen.

### 3. ❌ Workflow Shortcuts
- **NO "Quick Fixes":** Do not patch bugs without understanding the root cause.
- **NO New Libraries:** Do not add dependencies without providing a written architectural justification in your plan.
- **NO Magic Numbers/Strings:** Extract everything to constants or resources.

---

## 🏗️ Architectural Standards

### 1. Clean Architecture Layers
You must strictly adhere to this dependency flow:
`UI (Compose)` -> `ViewModel` -> `Domain (UseCase)` -> `Data (Repository)` -> `Remote/Local Source`

- **Domain Layer:** Pure Kotlin. No framework dependencies (no Android, no Compose).
- **Data Layer:** Handles data retrieval (Supabase, SQL). Maps DTOs to Domain Models.
- **Shared Module:** All *new* non-UI logic must go into `shared/src/commonMain`.
- **ViewModel (Android):** Uses `StateFlow` to expose state. Handles UI logic and interacts with UseCases.
- **View (Android):** Activities and Composables. Responsible for UI only.

### 2. Tech Stack Mandates
- **Networking:** Ktor Client + Supabase-kt.
- **DI:** Hilt (Android), Koin (Shared).
- **Async:** Coroutines & Flows (exclusively).
- **UI:** Jetpack Compose (Material3).
- **Serialization:** `kotlinx.serialization` (mandatory for KMP).

---

## 📝 How to Reason About Changes

Before writing code, ask yourself:
1.  **"Where does this live?"** -> If it's logic, it goes in `shared`. If it's UI, it goes in `app`.
2.  **"Is this migration-safe?"** -> Will this code need to be rewritten for KMP later? If yes, write it in KMP *now*.
3.  **"Am I breaking the pattern?"** -> Look at existing Feature packages. Mimic the directory structure exactly.

### Required Output Format
When presenting a solution:
1.  **Plan:** Step-by-step execution path.
2.  **Safety Check:** Explicitly state "Checked for KMP compatibility".
3.  **Code:** Full file content (no partial snippets unless modifying).
4.  **Verification:** Proof that the change compiles/runs (if applicable).

---

## 📂 Directory Structure Awareness
- `shared/src/commonMain/kotlin/...`: **ALL** core business logic, domain models, and repositories.
- `app/src/main/java/.../feature/...`: Android-specific UI (Screens, Components, ViewModels).
- `app/src/main/java/.../core/...`: Android-specific utilities (only if strictly necessary).
- `app/src/main/java/.../data`: Legacy Repositories/DAOs (Refactor target).
- `app/src/main/java/.../domain`: Legacy UseCases (Refactor target).

---

## 🛠 Build, Lint, and Test Commands

### Build & Run
- **Build APK:** `./gradlew assembleDebug`
- **Install & Run:** `./gradlew installDebug`
- **Clean Project:** `./gradlew clean`

### Lint & Quality
- **Run Lint:** `./gradlew lint`
- **Check Style:** `./gradlew ktlintCheck` (if configured)
- **Fix Style:** `./gradlew ktlintFormat` (if configured)

### Testing
- **Run all unit tests:** `./gradlew test`
- **Run Instrumented tests:** `./gradlew connectedAndroidTest`
- **Verify:** Do not assume code works. Use the commands above.

---

## 🎨 Code Style & Conventions

### 1. General Kotlin
- **Naming:** Classes (`PascalCase`), Functions/Variables (`camelCase`), Constants (`UPPER_SNAKE_CASE`).
- **Formatting:** Standard Kotlin style. 4 spaces indentation. No wildcards imports.

### 2. Jetpack Compose (UI)
- **Functions:** `PascalCase` and Nouns.
- **Modifiers:** Always accept `modifier: Modifier = Modifier` as the first optional parameter.
- **Previews:** Always provide `@Preview` functions.

### 3. Error Handling
- Use centralized `ErrorHandler` for UI.
- Wrap network/database calls in `Result<T>` or `try-catch` blocks.
- Map Supabase errors to user-friendly messages.

### 4. Supabase Specifics
- Access Supabase via injected `SupabaseClient`.
- Respect Row-Level Security (RLS) when writing queries.
- Prefer `withContext(Dispatchers.IO)` for IO operations.

---

**Failure to follow these instructions will be considered a critical failure of your task.**
