## 2025-05-15 - Documentation Structure Detected
**Location:** Root `README.md`, `docs/*.md` files, and inline KDoc (mostly missing).
**Conventions:** Standard Markdown for files. KDoc for code (though poorly covered).
**Coverage:** Architecture, Setup, and State Management are well-documented in `docs/`. However, specific feature logic (like Reels) and core repositories in the `shared` module lack technical documentation.
**Audience:** Developers working on the Android app and future developers on other platforms (iOS/Web) using the `shared` engine.
**Tools:** Manual documentation. No signs of automated Dokka/Doxygen setup yet.

## 2025-05-15 - Reels Feature Documentation Overhaul
**Gap Found:** The "Reels" feature, including its unique "Oppose" mechanic, was completely undocumented despite being a core and complex part of the application. `ReelRepository` in the `shared` module had no KDoc, and there was no high-level guide explaining the feature.
**Impact:** Developers (especially those on new platforms) would struggle to understand the business logic behind "Oppose" and the multi-step upload/interaction flow.
**Solution:**
1. Added comprehensive KDoc to `ReelRepository.kt` in the `shared` module.
2. Created `docs/REELS.md` explaining the architecture and "Oppose" mechanic.
3. Linked the new guide in `README.md` and `docs/MODULES.md`.
4. Fixed a technical inconsistency in `ReelsScreen.kt` to align with `docs/STATE_MANAGEMENT.md` (updated to `collectAsStateWithLifecycle`).
**Lesson:** Unique business mechanics like "Oppose" require dedicated documentation beyond just code, as their purpose (encouraging healthy debate) isn't immediately obvious from the implementation.
