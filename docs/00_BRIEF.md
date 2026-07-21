# Project Brief

## One-line pitch

Kriptic is an offline, phone-only safety toolkit — mesh chat, hazard maps, SOS, and a survival reference — for people who need to coordinate when the network has been shut off.

## The problem, in more detail

During protests, authorities in many regions deliberately throttle or shut down mobile data and even voice service in a geographic area, specifically to disrupt coordination. When this happens, people lose:

- The ability to tell each other where danger is (police lines, kettling, blocked roads)
- The ability to reach a friend who's separated from the group
- The ability to call for medical help or point people to safe zones / legal aid
- Basic reference information (legal rights, first aid) that would normally be a search away

Kriptic assumes the network is **hostile or absent by default**, not as an edge case. Every core feature must function with the phone in airplane mode, no SIM, and no Wi-Fi router in range.

## Who this is for

- Protesters and organizers who need situational awareness and a way to reach their group without cellular infrastructure
- Bystanders/medics/legal observers who need to broadcast or receive hazard and help information quickly
- Anyone in a crowd where phones are the only shared infrastructure available

This is **not** built for general-purpose offline messaging, disaster relief logistics, or enterprise mesh networking — those are adjacent but different problems with different threat models. Kriptic is scoped specifically to the protest/civil-unrest use case, starting with the Delhi NCR region.

## Design principles

1. **Offline-first is the architecture, not a fallback.** If a feature silently depends on internet access, it doesn't belong in the critical path.
2. **No accounts, no phone numbers.** Identity is a locally generated keypair plus a chosen username. Nothing ties a person's real identity to the app unless they choose to reveal it in conversation.
3. **Real over broad.** A feature that's fully functional but narrow beats a feature that's broad but half-working or mocked. No "coming soon" screens ship — cut the feature instead.
4. **Calm interface under a high-stress use case.** The UI should never look alarming or "techy" by default — clear thinking under pressure needs a calm surface, with urgency reserved for the moments that actually need it (SOS).
5. **Assume a hostile state actor as a threat model.** Transport-level encryption is not enough — every message is end-to-end encrypted at the app layer, independent of whatever the mesh transport already provides. Local storage is encrypted at rest. Panic wipe has no confirmation step, because the point is speed under duress.
6. **Build on proven open source, don't reinvent a mesh protocol from scratch.** See `docs/06_THIRD_PARTY.md`.

## Success criteria for v1

A v1 build is "done" when, with two or more physical Android phones all in airplane mode:

1. Every phone has picked a unique username and generated its identity keypair.
2. A message sent in a channel on one phone appears on all other phones in mesh range within a few seconds, including relayed via a third phone acting as a hop.
3. A marker dropped on the map on one phone appears on the others' maps within a few seconds, with the correct type and expiry behavior.
4. SOS triggered on one phone produces a visible, location-tagged alert on the others.
5. Panic wipe instantly and irreversibly clears local chat history, markers, and identity on the triggering phone.
6. The Knowledge tab returns accurate offline search results for first-aid and know-your-rights content with zero network calls.
7. The offline map renders Delhi NCR streets, landmarks, and shop names with zero network calls and zero runtime download step.

## Non-goals for v1

See `docs/03_SCOPE.md` for the full build/don't-build list. Headline exclusions: iOS, on-device LLM, any backend dependency for core features, group calls.
