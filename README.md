<div align="center">

# Synapse Social

### Next-Gen Social Network Client
*Native UI • Kotlin Multiplatform • Supabase Powered*

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose-4285F4?style=flat-square&logo=android&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=flat-square&logo=supabase&logoColor=white)

[Features](#-features) • [Architecture](#-architecture) • [Getting Started](#-getting-started) • [Documentation](#-documentation)

</div>

---

## 🚀 Features

- **Cross-Platform Core**: Logic shared between Android, iOS (planned), and Web via Kotlin Multiplatform.
- **Modern UI**: 100% Jetpack Compose for Android.
- **Secure Backend**: Powered by Supabase (Auth, Database, Storage, Edge Functions).
- **Privacy First**: Planned End-to-End Encryption.

## 🏗️ Architecture

Synapse uses a **Hybrid Architecture** leveraging the best of native UI and shared logic.

- **UI**: Native (Compose for Android, SwiftUI for iOS).
- **Logic**: Shared `shared` module (Networking, Repositories, Domain models).
- **Backend**: Supabase.

> See [Architecture Guide](docs/ARCHITECTURE.md) for a deep dive.

## 📚 Documentation

| Guide | Description |
| :--- | :--- |
| [**Setup Guide**](docs/SETUP.md) | 🛠️ Build and run instructions. |
| [**Architecture**](docs/ARCHITECTURE.md) | 🏗️ Clean Architecture & KMP details. |
| [**Modules**](docs/MODULES.md) | 📦 Project module breakdown. |
| [**Contributing**](docs/CONTRIBUTING.md) | 🤝 Contribution guidelines. |
| [**Code Style**](docs/CODESTYLE.md) | 🎨 Coding standards. |
| [**Roadmap**](docs/ROADMAP.md) | 🗺️ Future plans. |

> **🤖 For AI Agents:** Please refer to [AGENTS.md](AGENTS.md) before making changes.

## 🛠️ Getting Started

1.  **Clone**: `git clone https://github.com/your-org/synapse-android.git`
2.  **Open**: Android Studio (Ladybug or newer).
3.  **Configure**: Set up `gradle.properties` (see [Setup Guide](docs/SETUP.md)).
4.  **Run**: Launch the `app` configuration.

## 📂 Project Structure

- `app/`: Android App (UI + ViewModels)
- `shared/`: KMP Engine (Domain + Data)
- `iosApp/`: iOS App (SwiftUI)
- `docs/`: Detailed Documentation

---

<div align="center">
  <sub>Built with ❤️ by the Synapse Team</sub>
  <br>
  <sub>Licensed under <a href="LICENSE">AGPLv3</a></sub>
</div>
