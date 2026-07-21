# Scope: Build vs. Not-Yet

This is an ongoing open source project with multiple contributors, not a single-day sprint — but the same discipline that made the original hackathon plan work still applies: **a feature that is fully real but narrow beats a feature that is broad but half-working.** Nothing ships as a mocked or "coming soon" screen; cut the feature from a milestone entirely rather than ship a stub of it.

---

## BUILD — v1

| Feature | Notes |
|---|---|
| Username + keypair identity | First-launch flow, no phone number, immutable once set |
| Mesh chat, channel-based (General / Priority / Danger-Alert / Information) | Inherited from bitchat-android fork, extended with Kriptic's channel set |
| Multi-hop relay (store-and-forward, TTL, dedup) | Inherited from bitchat-android fork |
| App-level E2E encryption on messages | Inherited from bitchat-android fork (X25519 + AES-256-GCM) |
| Offline map of Delhi NCR with landmarks/shop names/POIs | Pre-bundled PMTiles from OSM extract, built via `map-data/` |
| Typed marker system (Danger / Safe / Police / Help / Gather) broadcast over mesh | Reuses the mesh envelope/relay pipeline |
| Silent SOS | Reuses the mesh pipeline at max priority/TTL |
| Panic wipe | Extends bitchat's existing wipe gesture |
| Offline Knowledge base (know-your-rights + first-aid), static content, full-text search | Real, reviewed content — not placeholder text; content lives in `content/`, editable independent of app code |

## NOT YET — explicitly deferred, not rejected

| Feature | Why it's deferred |
|---|---|
| On-device LLM for the Knowledge tab | Integrating a multi-GB quantized model and getting reliable, non-lagging inference across a wide range of real contributor/tester devices is a substantial project on its own, with real failure modes (crashes, battery drain, inconsistent answers on a safety-critical surface) that would undermine trust in the rest of the app if shipped before it's solid. Ship static, reviewed content first; the `knowledge/` module is architected so an LLM chat interface (candidates: SmolChat-Android, Google AI Edge Gallery — see `docs/06_THIRD_PARTY.md`) can be dropped in later without restructuring the rest of the app. |
| iOS app | bitchat's *protocol* is cross-platform, but a Kriptic iOS client is a separate, real build effort (different mesh/background-execution constraints on iOS) — not a v1 requirement when the primary deployment context (Delhi NCR protests) is majority-Android. |
| Backend server for any core feature | The whole point is zero infrastructure dependency. A backend is fine as an *optional, non-required* bonus path later (e.g. internet gateway relay) but must never be required for core features to work. |
| Global username uniqueness / central identity registry | v1's uniqueness guarantee is local-to-mesh-session only; a stronger guarantee would require some form of coordination service, which conflicts with the no-backend principle — worth a dedicated design discussion before building, not a default. |
| Internet gateway relay (a phone with real connectivity forwards queued mesh messages outward) | Real value, but genuinely optional — the app must be fully useful with zero devices ever regaining connectivity. |
| Group voice/video calls | Bandwidth-heavy and unreliable over BLE mesh; not core to the safety thesis. |
| Cross-region map bundles (beyond Delhi NCR) | v1 is scoped to one region done well. Multi-region support is a real feature (tile bundle selection, download-when-possible flow) worth designing deliberately rather than bolting on. |

## Rule of thumb for new feature proposals

When someone proposes a new feature, the questions to ask before adding it to a milestone:

1. Does it work with the phone in airplane mode, no SIM, no router in range? If not, it's not core — it can only ever be an optional bonus path.
2. Does it reuse the existing mesh/crypto pipeline, or does it require a second transport/trust mechanism? Prefer reuse.
3. Is there a contributor who can actually build *and* test it on real hardware (two-plus physical Android phones)? A feature nobody can test on real mesh conditions shouldn't be marked done.
4. If it touches the Knowledge base content (legal/first-aid), has it been reviewed by someone with relevant subject-matter knowledge, not just an engineer? Safety-critical content needs a content review step, not just a code review step — see `CONTRIBUTING.md`.
