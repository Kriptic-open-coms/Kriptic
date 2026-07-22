# Kriptic

[![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)](https://www.figma.com/design/MPjVu6LxPHDoTdPA6ERpBg/Untitled?node-id=0-1&t=wTowm68vFYEQdYcB-1)
[![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/CXybPutgp)

**A phone-only, off-grid safety toolkit for people at protests when cellular and internet infrastructure is deliberately shut down.**

No internet. No cell service. No external hardware. Just the phones already in people's pockets, talking to each other directly.

---

## The problem

When authorities shut down mobile networks and internet access during protests, they cut off far more than social media — they cut off the ability to coordinate safely, warn others of danger, find help, and access basic legal and medical knowledge. Kriptic restores that baseline capability using only what a stock Android phone already has: Bluetooth, Wi-Fi radios, GPS, and local storage.

## What Kriptic does (v1)

| Pillar | What it actually does |
|---|---|
| **Identity** | A one-time username, chosen on first launch, tied to a locally generated keypair. No phone number, no account, no server-side registration. Usernames can't collide and can't be changed. |
| **Mesh Comms** | Encrypted peer-to-peer messaging over Bluetooth LE mesh, hopping device to device with no internet or cell tower required. Organized into **channels** (General, Priority, Danger/Alert, Information, ...). |
| **Offline Map** | A pre-bundled, high-detail offline map of the Delhi NCR region — streets, landmarks, shop names, and POIs from OpenStreetMap data — with no download step needed at runtime. |
| **Marker System** | Drop a typed pin on the map (Danger, Safe, Police, Help, Gather, ...) with an optional description. Pins broadcast over the mesh so the whole crowd sees them, and expire automatically so stale data doesn't mislead people. |
| **Silent SOS** | A fast, low-friction trigger that broadcasts your location and status to nearby peers and asks for help — no need to fully unlock or navigate the app. |
| **Panic Wipe** | Instantly destroys local data. No confirmation dialog — the trigger *is* the confirmation. |
| **Knowledge Base** | A searchable, offline first-aid and know-your-rights reference for protest contexts (tear gas exposure, wound care, arrest/detention rights, etc.), organized by the same channel-style categories as the rest of the app. |

## Read next

1. [`docs/00_BRIEF.md`](docs/00_BRIEF.md) — the project brief: problem, users, principles, success criteria
2. [`docs/01_ARCHITECTURE.md`](docs/01_ARCHITECTURE.md) — what we're building on top of (fork of bitchat-android) and why
3. [`docs/02_PROJECT_STRUCTURE.md`](docs/02_PROJECT_STRUCTURE.md) — the monorepo layout
4. [`docs/03_SCOPE.md`](docs/03_SCOPE.md) — explicit build / don't-build-yet list for v1
5. [`docs/04_DESIGN_SYSTEM.md`](docs/04_DESIGN_SYSTEM.md) — the visual language
6. [`docs/05_ROADMAP.md`](docs/05_ROADMAP.md) — v1 → v2 → v3 milestones
7. [`docs/06_THIRD_PARTY.md`](docs/06_THIRD_PARTY.md) — every open-source project we build on, fork, or reference, and why
8. [`CONTRIBUTING.md`](CONTRIBUTING.md) — how to get set up and send a PR
9. [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)

## Tech stack summary

- **Base:** fork of [bitchat-android](https://github.com/permissionlesstech/bitchat-android) (Kotlin, Jetpack Compose, Material 3, MIT), stripped to its mesh + crypto core and rebuilt on top of
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3, custom-themed (see `docs/04_DESIGN_SYSTEM.md`)
- **Mesh transport:** Bluetooth LE mesh (inherited from bitchat's transport layer), multi-hop store-and-forward relay
- **Maps:** MapLibre Native Android SDK + offline PMTiles vector tiles built from OpenStreetMap Delhi NCR extracts
- **Local storage:** Room + SQLCipher (encrypted at rest), wiped on panic trigger
- **Min SDK:** 26 (Android 8.0)

## License

MIT — see [`LICENSE`](LICENSE). This matches the license of bitchat-android, the codebase Kriptic is forked from. Map data bundled with the app is derived from OpenStreetMap and is separately licensed under the **Open Database License (ODbL)**, which requires attribution and share-alike on the *data*, not the code — see `docs/06_THIRD_PARTY.md` for the exact attribution text to keep in the app.

## Status

Early-stage, active development. Not yet ready for real-world reliance — see `docs/03_SCOPE.md` for what's actually load-bearing today versus planned.
