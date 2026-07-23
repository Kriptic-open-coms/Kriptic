# Roadmap

This is a living document — update it as milestones land or priorities shift. Granular task tracking lives in GitHub Issues/Projects, not here; this file is the shape of the plan, not the task list.

## v1 — Core safety toolkit (current focus)

Goal: the six-point "done" checklist in `docs/00_BRIEF.md` passes on real hardware, in Delhi NCR, with content that's actually been reviewed rather than placeholder text.

- [ ] Fork + strip bitchat-android; written walkthrough of inherited architecture for contributors (blocks everything else)
- [ ] Security review of inherited mesh/crypto code before any real-world reliance
- [ ] Username + keypair onboarding flow
- [ ] Kriptic channel set wired into the inherited mesh/messaging UI
- [x] Offline Delhi NCR map pipeline (`map-data/`) producing a landmark/shop-name-rich PMTiles bundle
- [ ] Marker system: typed pins, mesh broadcast, expiry, aggregation
- [ ] Silent SOS trigger
- [ ] Panic wipe extended from bitchat's existing gesture
- [ ] Knowledge base: reviewed know-your-rights (Delhi NCR jurisdiction) + first-aid content, FTS search
- [ ] Design system applied consistently across all three tabs
- [ ] Two-plus-physical-device test pass of the full v1 checklist in `docs/00_BRIEF.md`

## v2 — Candidates (not committed, in rough priority order)

1. **On-device LLM for the Knowledge tab** — natural-language Q&A over the same reviewed content, using SmolChat-Android or Google AI Edge Gallery as the inference layer (see `docs/06_THIRD_PARTY.md`). Ships only once v1's static content is solid and the `knowledge/` module's LLM-ready seam has been validated.
2. **Duress PIN** — decoy empty state on a secondary unlock code.
3. **Internet gateway relay** — a device with real connectivity optionally forwards queued mesh messages outward. Strictly optional/bonus, never required.
4. **Stronger username-uniqueness guarantees** — needs a real design discussion given the no-backend principle; not a default assumption.
5. **Additional map regions** beyond Delhi NCR, with a deliberate region-bundle-selection UX.
6. **App disguise / stealth launcher icon** (calculator-style alias swap) — real, working Android pattern, deferred only because it wasn't in the current scope brief; revisit if contributors want it back in.

## v3 and beyond — not yet scoped

- iOS client (protocol-compatible with the Android mesh, per bitchat's existing cross-platform design, but a distinct build)
- Any of the above v2 items that got deferred further

## How to propose a change to this roadmap

Open an issue using the `feature_request` template, and run it through the four questions in `docs/03_SCOPE.md` before it gets added here.
