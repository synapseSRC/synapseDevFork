## 📌 Project Overview

You are working in a **social media platform monorepository** under **active development**.

### 🌍 Platform Vision
This platform is designed to be **widely available across operating systems**, including:

- Android
- iOS
- Windows
- macOS
- Linux
- PWA  
- *(and more in the future)*

> [!Note]
> **Current focus:** Android-first development  
> **Design principle:** Architecture must remain **future-proof** for other platforms.

---

## 🏗️ Architecture

### 🔹 Current Approach
- **Native UI per platform**
- **Shared Multiplatform SDK** for business logic & data

### 🎯 Long-Term Goal
- **Maximum code sharing** inside `shared`
- **Native performance** inside platform-specific `app`

> [!Tip]
> If logic can live in `shared`, it **must** live in `shared`.

---

## 🚫 STRICT PROHIBITIONS
> Violations here are **non-negotiable** and will block acceptance.

### 🧱 Architecture Rules
- ❌ **NO Direct Backend SDK Usage in Domain/UseCases**
  → All backend access through **Repository Interfaces**
  → Supabase/REST/BaaS clients stay in `data` layer only
- ❌ **NO Backend-Specific Types in Domain Layer**
  → Use **DTOs** for network responses
  → Use **Domain Models** for business logic
  → Require **Mapper** classes between layers
- ❌ **NO Hardcoded Backend Assumptions**
  → Design for swappable backends (REST, Supabase, custom like Signal)
  → Use **DataSource** abstractions (`SupabaseDataSource`, `RestApiDataSource`)
- ❌ **NO Android-only Room usage**  
  → Databases must live in `shared`  
  → Use **SQLDelight** or **Room KMP**
- ❌ **NO Business Logic in UI**  
  → Composables render **state only**
- ❌ **NO Business Logic in ViewModels**  
  → Delegate logic to **UseCases**
- ❌ **NO Platform Leaks**  
  → Never import `android.*` or `java.*` in `commonMain`

### 🔄 State Management Rules
- ❌ **NO Mutable State in Composables**  
  → Use `StateFlow` from ViewModels
- ❌ **NO God ViewModels**  
  → **One ViewModel per feature/screen**
- No cache should be committed to the repository

### 🏗️ Layer Boundaries (Production-Ready)
```
┌─────────────────────────────────────────────┐
│  UI Layer (app/)                            │
│  • ViewModels + Composables                 │
│  • NO business logic                        │
└─────────────────┬───────────────────────────┘
                  │ StateFlow
┌─────────────────▼───────────────────────────┐
│  Domain Layer (shared/domain/)              │
│  • UseCases (business logic)                │
│  • Domain Models (pure Kotlin)              │
│  • Repository Interfaces                    │
│  • NO backend SDK imports                   │
└─────────────────┬───────────────────────────┘
                  │ Repository Interface
┌─────────────────▼───────────────────────────┐
│  Data Layer (shared/data/)                  │
│  • Repository Implementations               │
│  • DataSource Abstractions                  │
│  • DTOs + Mappers                           │
│  • Backend SDKs (Supabase/Ktor/etc.)        │
└─────────────────────────────────────────────┘
```

### 📦 Dependency Rules
- **Domain** depends on: Nothing (pure Kotlin)
- **Data** depends on: Domain interfaces
- **UI** depends on: Domain (UseCases + Models)
- **Backend SDKs** live in: Data layer only

> [!Warning]
> Any PR violating these rules will be **rejected without review**.

---

## 📩 PRE-SUBMISSION  
### _(Packaging Stage – Mandatory)_

Before moving forward:

1. ✅ **Build MUST pass**
   - Do **not** start submission without a successful build.
2. 🔍 **Code Review MUST be completed**
   - Self-review or peer-review required.
3. 🧹 **Cache Files MUST NOT be committed**
   - Check for `.gradle/`, `build/`, `.idea/`, `*.iml`, `local.properties`, etc.
   - Run `git status` to verify no cache/build artifacts staged
4. 🚫 **NO Empty Commits**
   - Commits must contain meaningful changes
   - Use `git diff --cached` to verify staged changes exist

> [!Important]
> No build + no review + cache files + empty commits = **no submission**

---

## 📦 SUBMISSION  
### _(The Presentation Stage)_

After completing the assigned task:

🎁 **ALWAYS include a PRESENT** for the user.

### 🧾 Pull Request Template (Required)

```md
- **Title:** "[emoji + type]: [concise change summary]"
- **Description:**
  * 💡 What: What was added/changed (facts only)
  * 🎯 Why: Problem or motivation
  * 🔧 How: High-level implementation approach
  * 🧪 Tests: Tests added/updated or N/A
  * ♿ Accessibility: Impact or N/A
- **References:** Related issues, PRs, or N/A
```
> [!Tip]
> Keep PR description focused, and reviewable.
### 🍏 iOS Development
- **Crypto Implementation:** Use `CoreCrypto` (C API) via Kotlin/Native cinterop for cryptographic functions on iOS. It avoids the need for Swift/Objective-C bridges required by `CryptoKit`. Ensure to handle empty byte arrays safely (avoid pinning empty arrays) to prevent runtime crashes.
