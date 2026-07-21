# Kriptic

**A phone-only, off-grid safety toolkit for people at protests when cellular and internet infrastructure is deliberately shut down.**

No internet. No cell service. No external hardware. Just the phones already in people's pockets, talking to each other directly.

---

## The problem

When authorities shut down mobile networks and internet access during protests, they cut off far more than social media — they cut off the ability to coordinate safely, find medical help, warn others of danger, and reach legal support. Kriptic exists to restore that baseline capability using only what a stock Android phone already has: Bluetooth, Wi-Fi radios, GPS, and local storage.

## What Kriptic does

| Pillar | What it actually does |
|---|---|
| **Mesh Comms** | Encrypted peer-to-peer messaging over Bluetooth/Wi-Fi Direct, hopping device to device with no internet or cell tower required |
| **Offline Map** | Pre-downloaded street map with live, crowd-sourced hazard pins (police lines, blocked roads, safe zones) shared over the mesh |
| **Silent SOS** | One hardware-button press broadcasts your location + status to your group, no need to unlock the phone |
| **Panic Wipe & Disguise** | Instantly destroys local data and disguises the app as a calculator on the home screen |
| **Offline Know-Your-Rights** | Searchable legal and first-aid reference, works fully offline |

## What Kriptic is not

- Not a chat app that "also works offline sometimes" — offline-first is the whole architecture, not a fallback mode.
- Not dependent on any server we control. If our backend disappears, the app still works, because there is no backend in the critical path.
- Not carrying an on-device LLM in v1. We deliberately cut this — see `docs/03_SCOPE.md` for why.

## Read next

1. `docs/01_ARCHITECTURE.md` — what we're building on top of, and why each piece was chosen
2. `docs/02_PROJECT_STRUCTURE.md` — the actual folder/module layout for the Android Studio / Antigravity project
3. `docs/03_SCOPE.md` — explicit build / don't-build list, so nobody wanders mid-sprint
4. `docs/04_DESIGN_SYSTEM.md` — the visual language (Framer-inspired, one configurable accent color, one configurable font)
5. `docs/05_BUILD_PLAN.md` — hour-by-hour execution plan with tickets ready to hand to Gemini

## Tech stack summary

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 (custom-themed, not default Material look)
- **Mesh transport:** Android Nearby Connections API (Google Play Services — peer-to-peer, offline, encrypted; NOT the deprecated Nearby Messages API)
- **Maps:** MapLibre Native Android SDK + offline PMTiles/MBTiles vector tiles
- **Local storage:** Jetpack Room + SQLCipher (encrypted at rest), wiped on panic trigger
- **Min SDK:** 26 (Android 8.0) — covers the vast majority of real-world devices while keeping access to modern Bluetooth/crypto APIs

## License note

Kriptic itself should be released under a permissive or copyleft license consistent with the libraries it depends on (MapLibre is BSD-2-Clause; check Nearby Connections' Play Services ToS since it's a Google proprietary SDK, not open source — this matters for any redistribution claims in your pitch).
