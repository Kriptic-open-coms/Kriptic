# Project Structure

Standard Android Studio / Gradle layout, Kotlin + Jetpack Compose. This is the structure to create inside `D:/Projects/Kriptic` when you initialize the project in Antigravity.

```
Kriptic/
├── docs/                                   # This documentation set
│   ├── 01_ARCHITECTURE.md
│   ├── 02_PROJECT_STRUCTURE.md
│   ├── 03_SCOPE.md
│   ├── 04_DESIGN_SYSTEM.md
│   └── 05_BUILD_PLAN.md
│
├── app/
│   ├── build.gradle.kts                    # App-level dependencies (see below)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml          # activity-aliases for stealth mode live here
│   │   │   ├── assets/
│   │   │   │   ├── map/
│   │   │   │   │   └── city.pmtiles         # pre-built offline map tiles
│   │   │   │   ├── legal/
│   │   │   │   │   └── know_your_rights.json
│   │   │   │   └── firstaid/
│   │   │   │       └── first_aid.json
│   │   │   │
│   │   │   ├── java/com/kriptic/app/
│   │   │   │   ├── KripticApplication.kt    # app entry, initializes crypto/keystore
│   │   │   │   │
│   │   │   │   ├── mesh/                    # Pillar 1: mesh comms
│   │   │   │   │   ├── NearbyMeshManager.kt     # wraps Nearby Connections API
│   │   │   │   │   ├── MessageEnvelope.kt       # message data class + serialization
│   │   │   │   │   ├── MeshRouter.kt            # store-and-forward relay logic, TTL, dedup
│   │   │   │   │   └── CryptoService.kt         # keypair gen, encrypt/decrypt/sign/verify
│   │   │   │   │
│   │   │   │   ├── map/                     # Pillar 2: offline map
│   │   │   │   │   ├── MapScreen.kt
│   │   │   │   │   ├── HazardPin.kt
│   │   │   │   │   ├── HazardPinRepository.kt
│   │   │   │   │   └── HazardBroadcastBridge.kt # connects hazard pins to mesh layer
│   │   │   │   │
│   │   │   │   ├── sos/                     # Pillar 3: silent SOS
│   │   │   │   │   ├── SosButtonReceiver.kt     # hardware key combo listener
│   │   │   │   │   ├── SosPayload.kt
│   │   │   │   │   └── CheckInWorker.kt         # buddy check-in / dead-man's-switch
│   │   │   │   │
│   │   │   │   ├── security/                # Pillar 4: panic wipe + disguise
│   │   │   │   │   ├── PanicWipeManager.kt
│   │   │   │   │   ├── StealthModeController.kt # toggles activity-alias
│   │   │   │   │   └── DuressPinHandler.kt
│   │   │   │   │
│   │   │   │   ├── reference/                # Pillar 5: know-your-rights / first aid
│   │   │   │   │   ├── ReferenceRepository.kt
│   │   │   │   │   ├── ReferenceSearchScreen.kt
│   │   │   │   │   └── ReferenceDetailScreen.kt
│   │   │   │   │
│   │   │   │   ├── data/                     # Room database, DAOs, entities
│   │   │   │   │   ├── KripticDatabase.kt
│   │   │   │   │   ├── MessageDao.kt
│   │   │   │   │   ├── ContactDao.kt
│   │   │   │   │   └── entities/
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/                # DESIGN SYSTEM lives here
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── DesignTokens.kt   # <- single config point for accent + font
│   │   │   │   │   ├── components/           # shared composables (buttons, cards, etc)
│   │   │   │   │   ├── nav/                  # navigation graph
│   │   │   │   │   ├── chat/                 # mesh chat UI
│   │   │   │   │   ├── home/                 # main dashboard / calculator disguise
│   │   │   │   │   └── onboarding/
│   │   │   │   │
│   │   │   │   └── calculator/               # THE DISGUISE UI (must be fully functional as a real calculator)
│   │   │   │       └── CalculatorScreen.kt
│   │   │   │
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── colors.xml
│   │   │       │   └── strings.xml
│   │   │       └── mipmap/                   # calculator icon + real app icon (both needed)
│   │   │
│   │   └── test/                             # unit tests — at minimum: crypto, mesh routing/TTL logic
│   │
│   └── proguard-rules.pro
│
├── gradle/
├── build.gradle.kts                          # project-level
└── settings.gradle.kts
```

## Key Gradle dependencies to add

```kotlin
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.09.00")) // check latest BOM
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.navigation:navigation-compose")

    // Nearby Connections (mesh transport)
    implementation("com.google.android.gms:play-services-nearby:19.3.0") // check latest

    // Maps
    implementation("org.maplibre.gl:android-sdk:11.5.1") // check Maven Central for latest

    // Local encrypted storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Crypto (key management, signing, encryption)
    implementation("com.google.crypto.tink:tink-android:1.13.0") // or LazySodium if preferred

    // Background work (buddy check-in / dead-man's-switch)
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
```

> **Note for Gemini/whoever wires up Gradle:** always verify these version numbers against Maven Central at build time — dependency versions age fast and a stale version can silently break the build. Treat every version number above as "last known good," not gospel.
