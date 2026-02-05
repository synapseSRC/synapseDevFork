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
- ❌ **NO Retrofit**  
  → Use **Ktor + Supabase-kt**
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

> [!Important]
> No build + no review = **no submission**

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