# Scope: Build vs. Don't Build

Solo builder, ~6-8 hours, one shot. This document exists so that under time pressure, the answer to "should I add X" is already decided and nobody has to make that call while tired at hour 6.

## Rule of thumb
**A feature that is fully real but narrow beats a feature that is broad but fake.** Every screen in the shipped app must do the real thing when tapped. No mocked data pretending to be live. No "coming soon" screens — cut the feature entirely rather than ship a stub of it.

---

## BUILD (v1, today)

| Feature | Why it makes the cut |
|---|---|
| Mesh chat (1:1 + broadcast) over Nearby Connections | This is the core thesis of the app — it has to be real and it has to work between two physical test devices |
| Multi-hop relay (store-and-forward, TTL, dedup) | Without this it's just "Bluetooth chat," not "mesh" — but this is a bounded, well-scoped piece of logic, achievable solo |
| App-level E2E encryption on messages | Non-negotiable for the premise of the app (protecting people from a hostile state actor) — also genuinely not hard with Tink/Keystore |
| Offline map with pre-bundled tiles | High visual impact, moderate effort, real GPS works with zero connectivity already |
| Hazard pins shared over mesh | Reuses the mesh layer you already built — incremental cost, big narrative payoff |
| Silent SOS (hardware button → broadcast location+status to contacts) | Small, self-contained, huge emotional/demo impact |
| Panic wipe | Small, self-contained, a judge will ask about safety and you need a real answer |
| Stealth calculator disguise | Small, self-contained, extremely demo-able ("watch this — it's not actually a calculator") |
| Offline know-your-rights + first-aid reference (static searchable content) | Real content, real utility, zero infrastructure risk |

## DO NOT BUILD (today)

| Feature | Why it's cut |
|---|---|
| On-device LLM | Not because it's a bad idea long-term — because integrating a multi-GB quantized model, getting inference running reliably on a random test device, and having it not lag/crash during a live demo is a multi-day risk on its own. A crashing "flagship" feature does more damage to the pitch than not having it. Ship the static reference content instead; architect `reference/` module so an LLM chat interface can be dropped in later without restructuring anything. |
| Custom mesh protocol / Briar fork | Solo + unfamiliar large codebase + hours remaining = high risk of spending all your time reading code instead of writing it. Nearby Connections gets you 80% of the value for a fraction of the time cost. |
| iOS app | Different mesh networking constraints entirely; splitting focus cross-platform halves your effective build time for no demo benefit (you only need one working device pair on stage) |
| Server-side anything as a dependency for core features | The whole point is the app works with zero infrastructure. A backend server is fine as an *optional* bonus path (e.g., internet gateway relay) but must never be required for the demo to work |
| User accounts / phone number registration | Identity should be a local keypair, not tied to a phone number — this is also just less to build |
| Group video/voice calls | Bandwidth-heavy, unreliable over BLE/Wi-Fi Direct mesh, not core to the safety thesis |
| Cross-app testing on iOS/hardware you don't have | Test on whatever two Android devices you actually have access to today. Don't lose time chasing device compatibility edge cases you can't reproduce. |

## If you have spare time after all of the above works

In priority order:
1. Polish the panic wipe → make it a true single gesture, no confirmation screen
2. Add message TTL/expiry visual indicator in chat UI
3. Add a basic "gateway relay" — if one phone gets real internet, it forwards queued mesh messages out (huge "wow" if it works, totally fine to cut if it doesn't)
4. Add duress PIN (decoy empty state)
5. Stretch: swap the static reference content for a small on-device LLM (only if everything else is rock solid with 2+ hours still on the clock)
