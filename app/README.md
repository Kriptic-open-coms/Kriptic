# app/

The Android Studio / Gradle project. This starts life as a fork of [bitchat-android](https://github.com/permissionlesstech/bitchat-android) (MIT) — see `docs/adr/0001-fork-bitchat.md` for why, and `docs/02_PROJECT_STRUCTURE.md` for the target module layout once the fork is stripped and Kriptic's own modules (`identity/`, `map/`, `sos/`, `knowledge/`) are added.

## First-time setup

1. Follow the fork checklist (tracked in the `v1` milestone in `docs/05_ROADMAP.md`): fork bitchat-android, strip the Nostr/internet-fallback transport and IRC-style commands, land as the initial commit here with a written architecture walkthrough for the team.
2. `ui/theme/` already contains the design system files (`DesignTokens.kt`, `Color.kt`, `Type.kt`, `Theme.kt`) carried forward from the project's earlier build — wire these into the forked project's theming rather than using bitchat's original theme.
3. See root `CONTRIBUTING.md` for build requirements (Git LFS for map tiles, two physical devices for mesh/map/SOS testing).
