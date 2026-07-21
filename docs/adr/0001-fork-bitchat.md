# ADR 0001: Fork bitchat-android instead of building mesh from scratch

## Status
Accepted

## Context

Kriptic's earlier solo build (v1 hackathon sprint, see git history / `docs/archive/` if present) planned to build the mesh layer directly on Android's Nearby Connections API, reasoning that forking an unfamiliar large codebase (Briar, or an equivalent) under time pressure risked burning most of the available time just reading code.

Since then, the project has moved from a solo same-day sprint to an ongoing, contributor-based open source project, which changes the calculus:

- `bitchat-android` (permissionlesstech, MIT license) already implements BLE mesh discovery, multi-hop store-and-forward relay, app-level E2E encryption (X25519 + AES-256-GCM), channel-based messaging, a no-account identity model, and an emergency wipe gesture — independently solving most of what Kriptic's "Mesh Comms" and "Panic Wipe" pillars need.
- It's written in the same stack Kriptic already committed to (Kotlin, Jetpack Compose, Material 3).
- It's MIT-licensed, which is compatible with Kriptic's own MIT license with no copyleft obligations.
- With multiple contributors now available (rather than one person on a clock), reading and adapting an existing codebase is a reasonable and lower-risk investment than it was in the original solo-sprint context.

## Decision

Fork `bitchat-android`. Strip the Nostr/internet-fallback transport and IRC-style command surface (not relevant to Kriptic's use case). Keep and build on top of the mesh transport, crypto/identity layer, channel model, and wipe gesture.

## Consequences

- Kriptic's contribution guide must clearly document which parts of the codebase are "inherited from bitchat" versus "Kriptic-original," so contributors know where upstream bitchat fixes might be worth pulling in versus where Kriptic has diverged.
- We take on bitchat's existing security assumptions and any of its existing bugs — a security review of the inherited crypto/mesh code before relying on it for real protest use is a prerequisite, not a nice-to-have. Track this as an explicit early milestone (see `docs/05_ROADMAP.md`).
- We lose the "did it ourselves so we understand every line" property v1 valued — mitigated by requiring the fork's initial integration PR to include a written walkthrough of the inherited architecture for the team.
