# Third-Party Projects: What We Use, Fork, or Reference

## In active use (v1)

### [bitchat-android](https://github.com/permissionlesstech/bitchat-android)
- **License:** MIT
- **Role:** Forked as Kriptic's base. Provides the BLE mesh transport, multi-hop store-and-forward relay, app-level E2E encryption (X25519 + AES-256-GCM), channel-based messaging model, and emergency wipe gesture.
- **What we strip out:** the Nostr/internet-fallback transport and IRC-style slash commands, which aren't relevant to Kriptic's offline-only use case.
- **Why this over building mesh from scratch:** see `docs/adr/0001-fork-bitchat.md`.

### [OpenStreetMap](https://github.com/openstreetmap) / OSM data
- **License:** Data is **ODbL** (Open Database License) — separate from Kriptic's own MIT code license. ODbL requires attribution and share-alike on the data itself. Keep the required OSM attribution string visible in the app (Settings/About screen at minimum) and in this repo.
- **Role:** Source data for the Delhi NCR offline map extract. We build our own PMTiles bundle from an OSM extract via `map-data/` — see `docs/01_ARCHITECTURE.md` §3.

### [MapLibre Native](https://github.com/maplibre/maplibre-native)
- **License:** BSD-2-Clause
- **Role:** The map rendering SDK — renders the bundled PMTiles fully offline.

## Referenced, not used as a dependency (v1)

### [Briar](https://github.com/briar)
- **License:** GPLv3 (for the core), which would impose copyleft obligations on anything that links against it
- **Role in Kriptic:** reference reading only. Briar is a production-grade, mature offline-mesh secure messenger with a much larger scope than Kriptic needs (Tor integration, forums, blogs). We read its architecture for ideas but do not import its code, both because bitchat-android is a closer fit for our stack (Kotlin/Compose vs. Briar's own UI stack) and because pulling in GPLv3 code would force a license change for the whole project — see the license decision in `docs/00_BRIEF.md`.

## Candidates for v2 (not in v1)

### [SmolChat-Android](https://github.com/shubham0204/SmolChat-Android)
- **Role (candidate):** on-device GGUF LLM inference for a natural-language interface over the Knowledge base, once the static content in v1 is solid. See `docs/05_ROADMAP.md`.

### [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery)
- **Role (candidate):** alternative on-device ML/GenAI runtime to evaluate against SmolChat-Android for the same v2 Knowledge-tab use case. Not committed to either yet — worth a small bake-off before committing.

## Adding something to this list

If you're proposing a new open-source dependency or fork target, open a PR to this file with: what it's for, its license (and whether that license is compatible with Kriptic's MIT license — flag anything copyleft), and why it's a better fit than building the equivalent in-house or extending what's already listed here.
