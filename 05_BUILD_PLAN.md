# Build Plan — 7 Hour Solo Sprint

Checkpoint every hour. If a ticket isn't done at its checkpoint, cut scope inside that ticket rather than letting it bleed into the next hour — protect the schedule, not the feature.

Each ticket below is written to be pasted directly into Gemini in Antigravity as a task prompt. Paste the whole block including "Context" and "Acceptance Criteria" — Gemini works better with explicit done-conditions than vague asks.

---

### Ticket 1 (Hour 0–1): Project skeleton + design system wired in
```
Create a new Android Studio project: Kotlin, Jetpack Compose, min SDK 26, package
com.kriptic.app. Set up the module structure exactly as described in
docs/02_PROJECT_STRUCTURE.md. Add the Gradle dependencies listed there.

Copy in the theme files I've already written: ui/theme/DesignTokens.kt, Color.kt,
Type.kt, Theme.kt. Download Inter Regular/Medium/SemiBold .ttf files and place
them in res/font/ as inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf.

Build a minimal MainActivity that wraps its content in KripticTheme and shows a
placeholder home screen with the app name styled using the Title text style,
on a background using MaterialTheme.colorScheme.background.

Acceptance criteria: app builds and runs on a device/emulator, shows dark-mode
themed screen with correct accent color and Inter font visibly applied.
```

### Ticket 2 (Hour 1–3): Mesh comms — the core feature
```
Implement mesh/NearbyMeshManager.kt wrapping the Nearby Connections API
(com.google.android.gms:play-services-nearby). Implement advertising, discovery,
and connection lifecycle callbacks. Required permissions: BLUETOOTH_ADVERTISE,
BLUETOOTH_CONNECT, BLUETOOTH_SCAN, ACCESS_FINE_LOCATION, NEARBY_WIFI_DEVICES —
request all at runtime with a clear rationale screen.

Implement mesh/MessageEnvelope.kt: data class {id: String, senderPubKey: String,
ttl: Int, timestamp: Long, payload: ByteArray, signature: ByteArray}, with
kotlinx.serialization for encode/decode.

Implement mesh/CryptoService.kt: generate an X25519/Ed25519 keypair on first
launch (store in Android Keystore), expose encrypt(payload, recipientPubKey),
decrypt(...), sign(...), verify(...).

Implement mesh/MeshRouter.kt: on receiving a payload from any connected peer,
deserialize to MessageEnvelope, check against a rolling seen-message-ID buffer
(dedup), decrement TTL, drop if TTL <= 0 or already seen, otherwise deliver to
local UI AND re-broadcast to all other currently connected peers.

Build a basic chat UI (ui/chat/) — message list + text input — that sends
through MeshRouter and displays incoming messages in real time.

Acceptance criteria: install the app on two physical Android phones, put both
in airplane mode, confirm they discover each other and a message typed on one
appears on the other within a few seconds. This is the single most important
test of the entire project — do not move on until this works on real hardware.
```

### Ticket 3 (Hour 3–4): Offline map + hazard pins
```
Integrate MapLibre Native Android SDK (org.maplibre.gl:android-sdk). Bundle a
PMTiles/MBTiles extract of [YOUR CITY] in app/src/main/assets/map/. Build
map/MapScreen.kt showing the offline map centered on device GPS location (GPS
works with zero connectivity — do not gate this behind a network check).

Implement map/HazardPin.kt {lat, lon, type: WARNING|DANGER|SAFE, timestamp,
reporterPubKey, expiresAt}. Add a "drop pin" UI action that lets the user tap
the map and select a hazard type, broadcasting the pin as a MessageEnvelope
payload through the existing MeshRouter from Ticket 2.

Implement map/HazardBroadcastBridge.kt: when MeshRouter delivers a payload
tagged as a hazard pin type, decode it and add/merge it into the local pin
store, then render it on the map with the correct semantic color (Warning/
Danger/Safe from DesignTokens). Pins older than their expiresAt should be
filtered out of the rendered set.

Acceptance criteria: drop a pin on phone A with airplane mode on, see it appear
on phone B's map within a few seconds via the mesh connection from Ticket 2.
```

### Ticket 4 (Hour 4–5): Silent SOS + panic wipe + stealth disguise
```
Implement sos/SosButtonReceiver.kt: listen for a hardware volume-down x3 press
pattern (even with screen off/locked if possible via a foreground service).
On trigger, build a SosPayload {senderPubKey, lat, lon, timestamp, status} and
broadcast it through MeshRouter to all connected peers with maximum priority/TTL.
No confirmation dialog — the trigger IS the confirmation, by design.

Implement security/PanicWipeManager.kt: a single function wipe() that drops and
recreates the Room database, clears DataStore/SharedPreferences, and clears the
Keystore alias holding the identity keypair. Wire this to a specific gesture
(e.g. long-press on the app logo for 3 seconds) — again, no "are you sure" step.

Implement security/StealthModeController.kt using activity-alias entries in
AndroidManifest.xml: default launcher shows a fully functional calculator
(calculator/CalculatorScreen.kt — real arithmetic, not a fake prop) with a
generic icon/label. Entering a specific sequence (e.g. type a set 4-digit
number then press "=") calls setComponentEnabledSetting() to swap the enabled
alias to the real Kriptic icon/label and navigates to the real app.

Acceptance criteria: SOS button press on phone A produces a visible alert with
location on phone B. Panic wipe gesture instantly clears all local chat history
and hazard pins. App icon on the home screen is a working calculator until the
correct sequence is entered.
```

### Ticket 5 (Hour 5–6): Offline know-your-rights + first-aid reference
```
Create app/src/main/assets/legal/know_your_rights.json and
app/src/main/assets/firstaid/first_aid.json with real, well-researched content
(jurisdiction-specific rights during arrest/detention/search, and first-aid
steps for tear gas exposure, crush injury, wound care, heat/cold exposure —
this content needs to be accurate, not placeholder text).

Implement reference/ReferenceRepository.kt to load and index this content using
a Room FTS4/FTS5 virtual table for fast offline full-text search.

Build reference/ReferenceSearchScreen.kt (search bar + result list) and
ReferenceDetailScreen.kt (full article view), styled per the design system.

Acceptance criteria: with the device in airplane mode, search "tear gas" and
"arrest rights" and get accurate, instantly-returned results with no network
calls made anywhere in this flow.
```

### Hour 6–7: Integration, real-device testing, demo polish
```
- Full walkthrough on two physical phones, both in airplane mode, of every
  pillar in sequence: send a mesh message, drop a hazard pin, trigger SOS,
  search the reference library, test panic wipe, test stealth mode toggle.
- Fix any crashes found during this walkthrough — this is not the time for
  new features, only for making the existing five pillars reliable.
- Prepare the demo script: know exactly which two phones you're using, what
  order you'll tap through, and have a backup plan if Bluetooth pairing is
  flaky on stage (pre-pair/pre-discover before you go up, if the platform
  allows a warm connection state).
```

## What "done" looks like at the end of hour 7

Two Android phones, both in airplane mode, sitting next to each other:
1. Type a message on phone A, it appears on phone B.
2. Drop a hazard pin on the map on phone A, it appears on phone B.
3. Press the SOS combo on phone A, phone B shows an alert with location.
4. Search "know your rights" content on either phone, instant results.
5. Trigger panic wipe on phone A, local data is gone instantly.
6. Home screen icon on both phones is a working calculator until the unlock sequence is entered.

That is a full-fledged, working product — not a proof of concept.
