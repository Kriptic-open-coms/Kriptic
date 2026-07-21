# Architecture

## Design principle

Every core feature must work with the phone in **airplane mode, no SIM, no Wi-Fi router in range**. If a feature silently depends on internet access, it doesn't belong in Kriptic's critical path — it can exist as an optional bonus ("sync when connectivity returns") but never as a requirement.

---

## 1. Mesh Communication Layer

### What we're using: Android Nearby Connections API
- **Source:** Google Play Services (`com.google.android.gms:play-services-nearby`), documented at developers.google.com/nearby/connections/overview
- **Status:** This is the *current, actively supported* API. Do NOT confuse it with **Nearby Messages**, which Google deprecated in December 2023 and no longer functions — an easy mistake if you find older tutorials.
- **What it gives us out of the box:** peer-to-peer discovery and connection between nearby Android devices, fully offline, using a dynamic combination of Bluetooth Classic, BLE, and Wi-Fi Direct — the OS picks the best radio automatically. Connections are already encrypted end-to-end at the transport level.
- **Range:** roughly 30–100m depending on radio and environment; a crowd of people is a dense, constantly-reshuffling mesh, which is actually a favorable topology for multi-hop relay.

### What we build on top of it
Nearby Connections gives you point-to-point links between devices that discover each other — it does **not** give you multi-hop mesh routing, message persistence, or app-level encryption. We build that layer:

- **Message envelope:** every message is `{id, senderPubKey, ttl, timestamp, payloadEncrypted, signature}`
- **Store-and-forward relay:** each device keeps a small rolling buffer of recently-seen message IDs (to avoid re-broadcast loops) and relays any message it hasn't seen before to all currently-connected peers, decrementing TTL each hop.
- **App-level E2E encryption:** transport encryption from Nearby Connections is not sufficient on its own for a threat model involving a hostile state actor — we add our own layer using `libsodium` (via the `saltyrtc` or `LazySodium` Android bindings) or Android's own `androidx.security.crypto` + `Tink` for key management. Identity = a locally generated X25519/Ed25519 keypair, never tied to a phone number.
- **No central server required.** Optional: if any single device on the mesh gets a real internet connection, it can act as a gateway and relay queued messages outward — but this is a bonus feature, not a dependency.

### Why not Briar
Briar (github.com/briar/briar) solves this same problem and is production-grade, but its codebase is large and unfamiliar; forking it under a solo, same-day timeline risks burning most of the available hours just understanding its architecture before you can safely modify it. For a longer build, Briar is the stronger long-term foundation. For today: build directly on Nearby Connections, which is less code and fully documented by Google.

---

## 2. Offline Maps

### What we're using: MapLibre Native Android SDK
- **Source:** github.com/maplibre/maplibre-native (open source, BSD-2-Clause license)
- **Gradle dependency:** `org.maplibre.gl:android-sdk` — check Maven Central for the current version at build time (11.x/13.x line at time of writing)
- MapLibre is a community-maintained fork of the pre-monetization Mapbox GL Native SDK. It renders vector tiles fully offline once tiles are bundled.

### Offline tile data
- Use **PMTiles** or **MBTiles** format containing an OpenStreetMap extract of the target city/area, generated ahead of time via [protomaps.com](https://protomaps.com) build tools or `tilemaker`/`planetiler`.
- Bundle this file into `app/src/main/assets/map/` at build time. It ships inside the APK — no download step needed at runtime, which matters because your users may never have had connectivity to download anything in the first place.
- MapLibre's `OfflineManager` API can also be used if you want to let users pre-select/download a custom region before heading into a low-connectivity situation, as a secondary flow.

### Hazard pins
- A `HazardPin { lat, lon, type, timestamp, reporterPubKey, expiresAt }` object, broadcast over the same mesh message layer described above.
- Client-side aggregation: pins with matching type/location within a radius are merged/counted, giving basic Sybil resistance (one bad actor spamming fake pins is diluted by corroboration from multiple independent reporters).
- Pins auto-expire (e.g. 90 minutes) so stale hazard data doesn't mislead people hours later.

---

## 3. Local Storage & Encryption

- **Room** (Android's standard local DB layer) backed by **SQLCipher** for at-rest encryption of the message store, contacts, and hazard pin cache.
- **Android Keystore** for storing the device's identity keypair — hardware-backed where the device supports it, never exported in plaintext.
- **Panic wipe** = drop and recreate the encrypted database, clear `SharedPreferences`/`DataStore`, clear the Keystore alias. This should be a single, fast, synchronous operation triggered by gesture or hardware button combo — no confirmation dialog, no "are you sure," because the whole point is speed under duress.

---

## 4. App Disguise / Stealth Mode

- Android supports multiple `<activity-alias>` entries in the manifest, each with its own icon and label, where only one is `enabled` at a time via `PackageManager.setComponentEnabledSetting()`.
- Default disguised state: app appears as "Calculator" with a generic calculator icon. A specific tap sequence inside the fake calculator UI (e.g. entering a specific number sequence and pressing "=") unlocks the real app.
- This is a real, working Android pattern — not a hack — used by several existing secure-communication apps.

---

## 5. Know-Your-Rights / First-Aid Reference (v1, no LLM)

- A local, bundled JSON/SQLite dataset of legal rights (jurisdiction-specific — needs real content, not placeholder text) and first-aid procedures relevant to protest contexts (tear gas exposure, crush injury, wound care, heat/cold exposure).
- Simple on-device full-text search (Room supports FTS4/FTS5 virtual tables) — instant, no network, no model inference required.
- This is explicitly designed so it can be swapped for an on-device LLM chat interface later (see `03_SCOPE.md`) without changing the rest of the app.

---

## 6. What we are deliberately NOT building in v1

- On-device LLM (see `03_SCOPE.md` for rationale)
- Custom mesh protocol / Briar fork
- Any dependency on a backend server for core features
- iOS version (Nearby Connections mesh behavior differs significantly cross-platform; Android-only for this build)
