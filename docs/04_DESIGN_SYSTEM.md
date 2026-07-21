# Design System

## Direction

Framer-style: clean, quiet, confident. Lots of whitespace, one accent color used sparingly and deliberately, neutral grayscale doing most of the work, restrained typography with clear hierarchy. Nothing about the UI should look "techy" or alarming — this app needs to look calm and trustworthy even though what it's used for is high-stress. Calm interface = clear thinking under pressure.

The theme is implemented as real Kotlin code in `app/.../ui/theme/` (`DesignTokens.kt`, `Color.kt`, `Type.kt`, `Theme.kt`), carried forward from the earlier build — use these files directly, don't reinterpret them from scratch.

## Navigation

Per the current mockups, the app is a 3-tab bottom nav:

| Tab | Screen | Purpose |
|---|---|---|
| **Messaging** | Mesh chat, channel tabs (General / Priority / Danger-Alert / Information / ...) | Send/receive over the mesh |
| **Maps** | Offline Delhi NCR map, marker layer, "Drop a pin" flow with typed pin selection + description | Situational awareness |
| **Knowledge** | Search + filtered list (same channel-style tag filters), full article view | Offline reference content |

Channel tag chips are a shared component reused across Messaging and Knowledge — same visual treatment, same horizontal scroll pattern, so the app reads as one coherent tagging system rather than two different UI languages.

## Core rules

1. **One accent color.** Primary actions, active states, and the SOS trigger only. Never a large background fill or decorative element.
2. **Neutral grayscale palette** for 90% of the UI.
3. **One font family**, two weights in practice (Regular + Medium/Semibold). No more than 3 type sizes per screen.
4. **Generous spacing**, 8dp base unit (4dp allowed for tight inline spacing only).
5. **Rounded corners, consistently** — one corner-radius scale, not ad hoc per component.
6. **No pure black backgrounds, no pure white text** — off-black/off-white for reduced eye strain.
7. **Dark mode is the default.** Used at night, at protests; battery/OLED considerations favor it. Light mode exists and follows the same token structure.

## Configuration point

`DesignTokens.kt` is the single file to edit to reskin the entire app: `AccentColor` and `AppFontFamily`. Everything in `Color.kt`, `Type.kt`, `Theme.kt` derives from these two values — a hardcoded color anywhere else is a bug.

## Default values

- **Accent color:** `#4F7CFF` — a clear, confident blue that signals "safety/action" without reading as alarm-red or governmental.
- **Font:** **Inter** — open-source, variable-weight, the de facto standard for this look. Swappable to Manrope or Space Grotesk in `DesignTokens.kt` if wanted.

## Type scale

| Token | Size | Weight | Use |
|---|---|---|---|
| `Display` | 32sp | Semibold | Rare — SOS confirmation, big states |
| `Title` | 22sp | Semibold | Screen titles |
| `Heading` | 17sp | Medium | Section headers, card titles |
| `Body` | 15sp | Regular | Default text, chat messages, knowledge list items |
| `Caption` | 13sp | Regular | Timestamps, metadata, hints |

## Spacing scale (8dp base)
`4 / 8 / 16 / 24 / 32 / 48`

## Corner radius scale
`Small = 8dp` (chips, small buttons) · `Medium = 14dp` (cards, inputs, marker-type sheet) · `Large = 24dp` (sheets, modals)

## Component notes

- **Primary buttons:** accent fill, off-white text, medium radius, flat (no shadow).
- **Secondary buttons:** transparent/outline, 1px neutral border, accent-colored text.
- **SOS trigger:** the one deliberate exception to "accent used sparingly" — allowed to escalate visually (larger, bolder, subtle pulse), because this is the one moment urgency should read instantly.
- **Cards:** tonal elevation (slightly lighter surface color), not drop shadows.
- **Channel/tag chips:** pill-shaped, outline by default, filled with accent when active — shared component across Messaging and Knowledge tabs.
- **Map marker types:** semantic colors, not the accent color, with a visible legend on the Maps screen:
  - **Danger** — warning red/amber
  - **Police** — neutral amber/orange
  - **Safe** — calm green
  - **Help** — accent blue (this is the one marker type allowed to borrow the accent, since it's a direct request-for-help signal, same family as SOS)
  - **Gather** — neutral gray-blue
- **"Drop a pin" flow:** bottom-sheet pattern — tap map → pin-type chip selection → optional description field → confirm. Large radius (sheet token), no more than the 4 core actions visible without scrolling per the current mockup.

## Files carried forward from the earlier build

- `ui/theme/DesignTokens.kt`
- `ui/theme/Color.kt`
- `ui/theme/Type.kt`
- `ui/theme/Theme.kt`

To use a different font: place new `.ttf` files in `app/src/main/res/font/` and reference them in `Type.kt` — already structured as a one-line change.

## Reference

Figma: the working mockups this doc is derived from — see project Figma link (shared separately with contributors; not duplicated here since Figma is the live source of truth and this doc would go stale).
