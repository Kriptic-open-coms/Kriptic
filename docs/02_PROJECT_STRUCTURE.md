# Project Structure

Kriptic is a single monorepo: the Android app, the offline map data pipeline, the knowledge-base content, and the project docs all live together. This keeps a marker-format change, a map-tile update, and the app code that consumes them reviewable in one PR instead of coordinated across repos.

```
kriptic/
├── README.md
├── LICENSE
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
│
├── docs/
│   ├── 00_BRIEF.md
│   ├── 01_ARCHITECTURE.md
│   ├── 02_PROJECT_STRUCTURE.md          # this file
│   ├── 03_SCOPE.md
│   ├── 04_DESIGN_SYSTEM.md
│   ├── 05_ROADMAP.md
│   ├── 06_THIRD_PARTY.md
│   └── adr/                             # architecture decision records
│       └── 0001-fork-bitchat.md
│
├── app/                                  # Android Studio / Gradle project (fork of bitchat-android)
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/
│   │   │   ├── map/
│   │   │   │   └── delhi_ncr.pmtiles     # built by map-data/, committed via Git LFS
│   │   │   ├── legal/                    # symlink or build-copy of content/legal/
│   │   │   └── firstaid/                 # symlink or build-copy of content/firstaid/
│   │   │
│   │   └── java/com/kriptic/app/
│   │       ├── KripticApplication.kt
│   │       │
│   │       ├── identity/                 # Pillar: username + keypair
│   │       │   ├── UsernameRegistrationScreen.kt
│   │       │   └── IdentityRepository.kt
│   │       │
│   │       ├── mesh/                     # inherited from bitchat, adapted
│   │       │   ├── MeshManager.kt
│   │       │   ├── MessageEnvelope.kt
│   │       │   ├── MeshRouter.kt
│   │       │   ├── CryptoService.kt
│   │       │   └── ChannelRegistry.kt    # config-driven channel list
│   │       │
│   │       ├── map/                      # Pillar: offline map + markers
│   │       │   ├── MapScreen.kt
│   │       │   ├── Marker.kt
│   │       │   ├── MarkerRepository.kt
│   │       │   ├── MarkerType.kt         # Danger / Safe / Police / Help / Gather / ...
│   │       │   └── MarkerBroadcastBridge.kt
│   │       │
│   │       ├── sos/                      # Pillar: silent SOS
│   │       │   ├── SosTrigger.kt
│   │       │   └── SosPayload.kt
│   │       │
│   │       ├── security/                 # Pillar: panic wipe (extends bitchat's wipe gesture)
│   │       │   └── PanicWipeManager.kt
│   │       │
│   │       ├── knowledge/                # Pillar: know-your-rights / first-aid reference
│   │       │   ├── KnowledgeRepository.kt
│   │       │   ├── KnowledgeSearchScreen.kt
│   │       │   └── KnowledgeDetailScreen.kt
│   │       │
│   │       ├── data/                     # Room database, DAOs, entities
│   │       │
│   │       └── ui/
│   │           ├── theme/                # DESIGN SYSTEM — see docs/04_DESIGN_SYSTEM.md
│   │           │   ├── Color.kt
│   │           │   ├── Type.kt
│   │           │   ├── Theme.kt
│   │           │   └── DesignTokens.kt   # single config point: accent color + font
│   │           ├── components/
│   │           ├── nav/                  # bottom nav: Messages / Maps / Knowledge
│   │           ├── messaging/
│   │           └── onboarding/
│   │
│   └── src/test/                         # unit tests — minimum: crypto, mesh routing/TTL, marker expiry
│
├── map-data/                             # offline tile build pipeline (not shipped in the APK source, produces the .pmtiles that is)
│   ├── README.md                         # how to regenerate delhi_ncr.pmtiles
│   ├── sources.md                        # exact OSM extract URLs/dates used, for reproducibility
│   └── scripts/
│       └── build_tiles.sh
│
├── content/                               # Knowledge base source content — editable by non-engineers
│   ├── legal/
│   │   └── know_your_rights.json
│   └── firstaid/
│       └── first_aid.json
│
└── .github/
    ├── ISSUE_TEMPLATE/
    │   ├── bug_report.md
    │   ├── feature_request.md
    │   └── content_change.md              # for legal/first-aid content edits specifically
    └── PULL_REQUEST_TEMPLATE.md
```

## Key Gradle dependencies (in addition to what's inherited from the bitchat-android fork)

```kotlin
dependencies {
    // Compose (already present in bitchat-android, listed for completeness)
    implementation(platform("androidx.compose:compose-bom:2024.09.00")) // check latest BOM at build time
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose")

    // Maps
    implementation("org.maplibre.gl:android-sdk:11.5.1") // check Maven Central for latest

    // Local encrypted storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
}
```

> Treat every version number above as "last known good," not gospel — verify against Maven Central at build time.

## Why content lives outside `app/`

`content/legal/` and `content/firstaid/` are pulled into the app assets at build time rather than hand-edited inside `app/src/main/assets/`. This means a lawyer or medic reviewing/updating the Knowledge base content doesn't need to touch Kotlin code or understand the Android project to send a PR — see `CONTRIBUTING.md`.
