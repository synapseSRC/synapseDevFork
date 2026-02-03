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

## 2025-05-25 - E2EE / Signal Protocol Documentation
**Gap Found:** The End-to-End Encryption (E2EE) implementation using the Signal Protocol was partially implemented but lacked any technical documentation or KDoc. This is a highly complex area involving X3DH key exchange and Double Ratchet messaging.
**Impact:** New developers would find it extremely difficult to understand the security model, key storage mechanisms (EncryptedSharedPreferences), or how to interact with the `SignalProtocolManager`.
**Solution:**
1. Created `docs/SECURITY_E2EE.md` explaining the X3DH flow, architecture, and security considerations.
2. Added comprehensive KDoc to `SignalProtocolManager.kt`, `AndroidSignalProtocolManager.kt`, and `SignalRepository.kt`.
3. Updated `README.md` and `docs/ROADMAP.md` to reflect the active development status of E2EE.
**Lesson:** Security-critical features like E2EE must be documented early to ensure the implementation stays aligned with the intended protocol and to provide a clear guide for platform-specific implementations (e.g., iOS).
