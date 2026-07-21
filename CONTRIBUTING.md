# Contributing to Kriptic

Thanks for being here. Kriptic is safety-critical software used under real pressure — the bar for "working" is higher than a typical app, and testing on real hardware (not just an emulator) matters more than usual. This guide covers how to get set up, how we work, and what "done" means for a PR.

## Before you start

1. Read `docs/00_BRIEF.md`, `docs/01_ARCHITECTURE.md`, and `docs/03_SCOPE.md` — they'll answer "why does it work this way" and "is this in scope" for most questions before you ask them.
2. Join [project chat — add link] to find an area to work on and avoid duplicate effort on the same ticket.
3. Check `docs/05_ROADMAP.md` and open GitHub Issues before starting anything not already tracked — for a project at this stage, an untracked PR is more likely to conflict with someone else's in-flight work.

## What you need locally

- Android Studio (current stable) with an emulator **or**, strongly preferred, two physical Android devices (min SDK 26 / Android 8.0+) with Bluetooth — mesh and marker features cannot be meaningfully tested on a single device or emulator alone.
- The repo cloned with Git LFS enabled (map tile bundles in `app/src/main/assets/map/` are stored via LFS — `git lfs install` before cloning if you haven't used LFS before).

```bash
git clone https://github.com/<org>/kriptic.git
cd kriptic
git lfs pull
```

Open `app/` in Android Studio as the project root.

## Ways to contribute (you don't have to write Kotlin)

- **Code:** mesh/crypto, map/marker UI, SOS, panic wipe, knowledge search, design system implementation.
- **Content:** know-your-rights and first-aid content in `content/legal/` and `content/firstaid/` — plain JSON, no Android toolchain needed. This needs subject-matter accuracy (legal/medical), not just clean prose — see the content review process below.
- **Map data:** improving the Delhi NCR extract/build pipeline in `map-data/` — landmarks and shop-name coverage matter as much as street accuracy for this use case.
- **Design:** the Figma file is the source of truth for visual design; PRs implementing new screens should link the relevant Figma frame.
- **Docs:** anything in `docs/` — especially the ADR log (`docs/adr/`) when a real architectural decision gets made or revisited.

## Branching and PRs

- `main` is always in a working, buildable state.
- Branch naming: `feature/<short-name>`, `fix/<short-name>`, `content/<short-name>`, `docs/<short-name>`.
- Open a PR early (draft is fine) rather than a single large PR at the end — easier to catch scope drift against `docs/03_SCOPE.md` early.
- Fill out the PR template (`.github/PULL_REQUEST_TEMPLATE.md`) — in particular, the "how was this tested" section. For anything touching `mesh/`, `map/`, `sos/`, or `security/`, "tested on two physical devices" is not optional.
- At least one review required before merge. For changes to `mesh/` or `security/` (the safety-critical core), two reviews.

## Content review process (legal / first-aid)

Changes to `content/legal/` or `content/firstaid/` need sign-off from someone with relevant subject-matter background before merge — a lawyer/legal-aid volunteer for `legal/`, a medic/first-aid-certified reviewer for `firstaid/`. Tag such PRs with the `content-review-needed` label and don't merge until that review lands, even if the code/format review is otherwise clean. Use the `content_change` issue template to propose substantive content edits before writing the PR if you're not sure the change is uncontroversial.

## Code style

- Kotlin: follow the existing conventions in the bitchat-android fork for anything in `mesh/` (consistency with upstream matters there); standard Kotlin/Compose idioms elsewhere.
- No hardcoded colors/fonts outside `ui/theme/DesignTokens.kt` — see `docs/04_DESIGN_SYSTEM.md`.
- Every PR touching `mesh/`, `map/`, `sos/`, or `security/` needs at least a minimal unit test where the logic is testable in isolation (TTL/dedup logic, marker expiry, etc.) — full mesh behavior itself needs real-device testing, which unit tests can't substitute for.

## Reporting security issues

Do not open a public issue for a security vulnerability (e.g. a way to deanonymize a user, break the E2E encryption, or defeat the panic wipe). Email [security contact — add address] instead. See `docs/01_ARCHITECTURE.md` for the parts of the codebase inherited from bitchat-android, since a vulnerability there may also need reporting upstream.

## Code of Conduct

By participating, you agree to abide by `CODE_OF_CONDUCT.md`.
