# Design System

## Direction

Framer-style: clean, quiet, confident. Lots of whitespace, one accent color used sparingly and deliberately (not everywhere), neutral grayscale doing most of the work, restrained typography with clear hierarchy. Nothing about the UI should look "techy" or alarming — this app needs to look calm and trustworthy even though what it's used for is high-stress. Calm interface = clear thinking under pressure.

Everything below is implemented as real Kotlin code in `ui/theme/`, not just guidance — Gemini should use these files directly, not reinterpret them.

## Core rules

1. **One accent color.** Used for primary actions, active states, and the SOS trigger only. Never used for large background fills or decorative purposes.
2. **Neutral grayscale palette** for 90% of the UI — backgrounds, cards, borders, secondary text.
3. **One font family**, two weights in practice (Regular + Medium/Semibold). No more than 3 type sizes on any single screen.
4. **Generous spacing.** Base unit = 8dp. All padding/margins are multiples of 8 (4 allowed for tight inline spacing only).
5. **Rounded corners, consistently.** One corner-radius scale (small/medium/large), not ad hoc per-component values.
6. **No pure black backgrounds, no pure white text.** Use off-black (`#111111`-ish) and off-white for reduced eye strain and a softer, more premium feel — this is a real Framer/modern-app convention, not just taste.
7. **Dark mode is the default**, not an afterthought — this app is used at night, at protests, and battery/OLED considerations favor dark UI. Light mode should still exist and follow the same token structure.

## Configuration point

**`DesignTokens.kt`** is the single file to edit to reskin the entire app. It defines:
- `AccentColor` — the one configurable accent color (default provided below)
- `AppFontFamily` — the one configurable font (default provided below)

Everything else in `Color.kt`, `Type.kt`, and `Theme.kt` derives from these two values. Nobody should hunt through the codebase for hardcoded colors — if a color isn't coming from `DesignTokens`/`Color.kt`, that's a bug.

## Default values

- **Accent color:** `#4F7CFF` (a clear, confident blue — signals "safety/action" without reading as alarm-red or governmental). Easy to swap to a different single hex in `DesignTokens.kt`.
- **Font:** **Inter** (open-source, free, the de facto standard for this exact clean/modern/Framer-adjacent look, excellent variable-weight support, easy to bundle). Configurable in `DesignTokens.kt` if you want to swap to something else (e.g. **Manrope** or **Space Grotesk** are good alternatives in the same family of taste).

## Type scale

| Token | Size | Weight | Use |
|---|---|---|---|
| `Display` | 32sp | Semibold | Rare — SOS confirmation screen, big states only |
| `Title` | 22sp | Semibold | Screen titles |
| `Heading` | 17sp | Medium | Section headers, card titles |
| `Body` | 15sp | Regular | Default text, chat messages |
| `Caption` | 13sp | Regular | Timestamps, metadata, hints |

## Spacing scale (8dp base)

`4 / 8 / 16 / 24 / 32 / 48`

## Corner radius scale

`Small = 8dp` (chips, small buttons) · `Medium = 14dp` (cards, inputs) · `Large = 24dp` (sheets, modals)

## Component notes

- **Primary buttons:** accent color fill, off-white text, medium radius, no shadow (flat, confident — not skeuomorphic).
- **Secondary buttons:** transparent/outline, 1px neutral border, accent-colored text.
- **SOS button:** the one exception to "accent used sparingly" — this is the single moment the UI is allowed to escalate visually (larger, bolder, possibly a subtle pulse animation), because it's the one moment urgency should read instantly.
- **Cards:** subtle elevation via a slightly lighter surface color, not drop shadows — flat design, tonal elevation (this is standard Material 3 practice and also very Framer-like).
- **Map hazard pins:** use semantic colors (not the accent) — e.g. warning amber for police/barricade, neutral gray for informational, calm green for safe zones/legal aid. Keep a legend visible.

## Files delivered alongside this doc

- `ui/theme/DesignTokens.kt` — accent color + font config point
- `ui/theme/Color.kt` — full derived palette (light + dark)
- `ui/theme/Type.kt` — Compose `Typography` object using the type scale above
- `ui/theme/Theme.kt` — the `KripticTheme` composable wrapping Material 3

To use a different font: download the Inter (or chosen alternative) `.ttf` files, place them in `app/src/main/res/font/`, and reference them in `Type.kt` — the file is already structured to make this a one-line change.
