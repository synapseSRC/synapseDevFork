# Synapse Social - Universal Engine Roadmap (KMP & E2EE)

This document tracks the migration of Synapse Social from a native Android application to a **WhatsApp-style** architecture: **Shared KMP Engine** for core logic/security and **100% Native UIs** for each platform.

## 🎯 Vision
Build a high-performance, memory-safe "Engine" in Kotlin (LLVM) that handles business logic, networking, and E2EE once, powering native luxury UIs for Android, iOS, and Web.

---

## 🏗️ Architecture Overview

| Layer | Technology | Status |
| :--- | :--- | :--- |
| **Networking** | Ktor + Supabase-kt | 🟡 Pending Migration |
| **Database** | Room Multiplatform + SQLCipher | 🔴 Pending Refactor |
| **DI** | Koin (Shared Engine) + Hilt (Android UI) | 🔴 Pending Setup |
| **Security/E2EE** | Double Ratchet (Signal Protocol) | 🟡 Implementation Phase (Android) |
| **Settings** | Multiplatform Settings (Keychain/EncryptedPrefs) | 🔴 Pending Setup |
| **Backend** | Supabase (Scalable to 100M+ users) | 🟢 Approved |

---

## 🚀 Scalability & Optimization Strategy
- **Horizontal Scaling:** Backend agnostic interfaces to support "Cellular" sharding if user base exceeds 100M.
- **Group Optimization:** Implementation of "Sender Keys" to minimize client-side battery drain.
- **Web Performance:** Kotlin/Wasm for the shared engine on Web to ensure native-speed encryption.

---

## 🛤️ Implementation Phases

### Phase 1: Infrastructure & Scaffolding
- [x] Create `:shared` module.
- [x] Configure targets: `android`, `ios` (XCFramework), `wasmJs` (Web - Disabled temporarily for stability).
- [x] Implement `BuildKonfig` (via SynapseConfig bridge) for cross-platform secrets.
- [x] Setup `Koin` for shared dependency injection (Dependency added).
- [x] Transition logging to `Napier`.

### Phase 2: Identity & Authentication (The "Engine" Core)
- [x] Migrate DTOs and Domain Models to `commonMain`.
- [x] Move `SupabaseClient` and Auth Logic to `shared`.
- [x] Implement Multiplatform `TokenManager` (Keychain/EncryptedPrefs).
- [ ] Connect Android UI (Compose) to Shared Auth Repository via Hilt bridge. (Deferred per user request)

### Phase 3: Data Persistence & Encryption
- [x] Refactor Room Database to `commonMain` (Code implemented in `SharedDatabase.kt`).
- [ ] Enable KSP for Room KMP (Blocked by Kotlin/KSP version mismatch in environment).
- [ ] Implement Encrypted SQLite drivers for iOS/Android.
- [x] **E2EE Identity:** Generate/Store device-specific identity keys on first run. (Android implemented)
- [ ] Implement E2EE Metadata encryption for media.

### Phase 4: Multi-Platform UI Implementation (Native Luxury)
- [ ] **Android:** Maintain current Compose UI (Zero-regression). Splash screen verified.
- [x] **iOS:** Scaffold native **SwiftUI** project linked to `:shared`. Splash screen implemented.
- [ ] **Web:** Scaffold **React/Wasm** project linked to `:shared`.

---

## 🔒 E2EE Strategy (Signal-Inspired)
- **Protocol:** Signal-style Double Ratchet.
- **Keys:** X25519 for handshakes, AES-GCM for payloads.
- **Group Logic:** Server-side fan-out of encrypted keys (Sender Keys).
- **Media:** Encryption of byte-streams before upload to Supabase/Cloudinary.

---

## 🛠 Progress Log
- **2026-01-18:** Initial Roadmap created. Architecture strategy finalized (WhatsApp-style: Shared KMP Engine + Native UIs).
- **2026-01-18:** Scalability strategy for 100M+ users finalized (Sharding + Edge Functions).
- **2026-01-18:** **Phase 1 & 2 Complete.** Shared Engine created (`:shared`) with Auth, Network, and Config. Android & iOS targets compiling successfully. Room KMP code implemented but KSP disabled due to environment limits.
- **2025-05-25:** **E2EE Implementation Started.** `SignalProtocolManager` and `AndroidSignalStore` implemented using `libsignal`. X3DH key bundle exchange logic added to `SignalRepository`. Initial security documentation created in `docs/SECURITY_E2EE.md`.
