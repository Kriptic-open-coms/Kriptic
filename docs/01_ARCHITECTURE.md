# Architecture

## Design principle

Every core feature must work with the phone in **airplane mode, no SIM, no Wi-Fi router in range**. If a feature silently depends on internet access, it doesn't belong in Kriptic's critical path.

---

## 0. Foundation: forking bitchat-android

We are **not** building the mesh layer from scratch. [`bitchat-android`](https://github.com/permissionlesstech/bitchat-android) (MIT license) is a working, actively maintained Kotlin + Jetpack Compose + Material 3 app that already implements:

- BLE mesh peer discovery and multi-hop relay (store-and-forward, up to 7 hops)
- App-level end-to-end encryption (X25519 key exchange + AES-256-GCM) — independent of whatever the BLE transport itself provides
- Channel-based group messaging
- No-account, no-phone-number identity model
- An emergency wipe gesture

This overlaps almost entirely with what Kriptic's "Mesh Comms" and "Panic Wipe" pillars need. Rather than re-deriving this (which was v1's original, riskier plan — see `docs/adr/0001-fork-bitchat.md`), Kriptic forks bitchat-android and:

1. **Keeps:** the BLE mesh transport, the crypto/identity layer, the channel model, the store-and-forward relay logic, the emergency wipe primitive.
2. **Strips:** bitchat's Nostr/internet-fallback transport, IRC-style slash commands, and any UI not relevant to Kriptic's use case, to keep the codebase legible for new contributors.
3. **Adds:** the username registration flow, the offline map + marker system, silent SOS (adapted from the wipe gesture pattern), and the Knowledge base.

Keeping the fork's diff from upstream as small and well-documented as possible matters — see `CONTRIBUTING.md` for how we track upstream-vs-Kriptic changes.

---

## 1. Identity & Usernames

- On first launch, the user picks a username. Uniqueness is enforced locally within any mesh session (collision check against all currently-visible peers) — there is no central registry, so global uniqueness across the whole userbase is *not* guaranteed by v1's design, only uniqueness-in-practice within a connected mesh. This constraint and its limits should be stated plainly in the onboarding UI.
- Username is bound to a locally generated X25519/Ed25519 keypair (inherited from bitchat's identity model), stored in the Android Keystore.
- Once set, neither the username nor the keypair can be changed — the user must reinstall to get a new identity. This is intentional: it keeps peer reputation/trust legible within a mesh session and discourages spoofing/rotation abuse.

## 2. Mesh Communication Layer (inherited + extended)

- Transport: Bluetooth LE mesh (bitchat's existing implementation).
- Message envelope, TTL, dedup, and relay logic: inherited from bitchat, audited and documented as part of the fork (see `docs/adr/0001-fork-bitchat.md`).
- Channels: General, Priority, Danger/Alert, Information by default (per the current mockups) — channel list should be config-driven, not hardcoded, so it's easy to add region- or event-specific channels later.
- Markers (below) and SOS payloads reuse this same envelope/relay pipeline rather than inventing a second transport path.

## 3. Offline Maps

- **Renderer:** MapLibre Native Android SDK (`org.maplibre.gl:android-sdk`, BSD-2-Clause, github.com/maplibre/maplibre-native).
- **Tile data:** PMTiles/MBTiles vector tile extract of Delhi NCR, built from OpenStreetMap data via `planetiler` or `tilemaker`. Includes streets, landmarks, shop names, and other POIs — a plain road-network extract is not sufficient for this use case.
- Tiles are bundled into the APK at build time (`app/src/main/assets/map/`); no runtime download step, because users may never have had connectivity to download anything in the first place. `map-data/` (repo root) holds the build pipeline that produces this bundle — see `docs/02_PROJECT_STRUCTURE.md`.
- OSM data is ODbL-licensed — attribution requirements apply to the data, independent of Kriptic's own MIT code license. See `docs/06_THIRD_PARTY.md`.

## 4. Marker System

- `Marker { lat, lon, type, description, timestamp, reporterPubKey, expiresAt }`, where `type` is one of the tag set shown in the mockups (Danger, Safe, Police, Help, Gather — extensible).
- Broadcast as a payload over the same mesh envelope described in section 2.
- Client-side aggregation: markers of the same type within a small radius are merged/corroborated, giving basic resistance to a single bad actor spamming fake markers.
- Auto-expiry (default ~90 minutes, tunable) so stale hazard data doesn't mislead people hours later.

## 5. Silent SOS

- A low-friction trigger (exact gesture TBD by implementers — hardware button combo, long-press, or similar) that broadcasts `{senderPubKey, lat, lon, timestamp, status}` at maximum relay priority/TTL through the existing mesh pipeline.
- No confirmation dialog. The trigger is the confirmation.
- Should reuse, not duplicate, bitchat's existing "fast trigger" pattern from its emergency wipe gesture.

## 6. Local Storage & Encryption

- Room, backed by SQLCipher, for message store, channel state, and marker cache.
- Android Keystore for the identity keypair — hardware-backed where the device supports it.
- **Panic wipe:** drop and recreate the encrypted database, clear preferences/DataStore, clear the Keystore alias. Single fast synchronous operation, no confirmation step — extends bitchat's existing wipe gesture rather than replacing it.

## 7. Knowledge Base (v1: static, no LLM)

- Bundled JSON/SQLite dataset: know-your-rights content (jurisdiction: India / Delhi NCR — needs real, reviewed legal content, not placeholder text) and first-aid procedures relevant to protest contexts (tear gas exposure, crush injury, wound care, heat/cold exposure).
- Room FTS4/FTS5 virtual table for instant offline full-text search.
- Content lives in `content/legal/` and `content/firstaid/` at the repo root (not buried inside the app module) so non-engineer contributors can edit it directly — see `docs/02_PROJECT_STRUCTURE.md` and `CONTRIBUTING.md`.
- Deliberately architected so this module can be swapped for an on-device LLM chat interface later (SmolChat-Android or Google AI Edge Gallery — see `docs/06_THIRD_PARTY.md`) without restructuring the rest of the app. That swap is out of scope for v1 — see `docs/03_SCOPE.md`.

## 8. What v1 deliberately does not include

- On-device LLM
- iOS (bitchat's protocol is cross-platform-compatible at the wire level, but a Kriptic iOS client is a separate build effort — not v1)
- Any backend server dependency for core features
- Group voice/video calls
- Internet gateway relay (a device with real connectivity forwarding queued mesh messages outward) — plausible v2 bonus, not required for v1

See `docs/03_SCOPE.md` for the full reasoning and the priority-ordered "if there's time" list.
