# ADR 0004: Channel & Marker Data Model

## Status

Accepted

## Context

The mesh carries different types of data: chat messages, map markers, SOS alerts, and system messages (peer join/leave, heartbeat). Each type has different delivery guarantees, priority, and rendering requirements. A unified data model keeps the protocol simple while allowing receivers to handle each type correctly.

## Decision

### Channel model

Channels are the organizational unit for chat messages. The default channel set is:

| Channel ID | Name | Purpose | Default Joined |
|---|---|---|---|
| `general` | General | Open discussion | Yes |
| `priority` | Priority | Time-sensitive coordination | Yes |
| `danger` | Danger/Alert | Immediate hazard warnings | Yes |
| `info` | Information | Useful updates, resources | Yes |

Channel list is **config-driven** via a JSON file bundled in assets, not hardcoded in Kotlin. This allows adding region- or event-specific channels without an app update.

### Message payload format (after decryption)

```json
{
  "type": "chat",
  "channel": "general",
  "body": "Stay safe near Gate 4",
  "timestamp": 1712345678000
}
```

### Marker payload format

```json
{
  "type": "marker",
  "lat": 28.6128,
  "lon": 77.2295,
  "marker_type": "police",
  "description": "Checkpoint at entry",
  "timestamp": 1712345678000,
  "expires_at": 1712350278000
}
```

Marker types and their semantic colors (for map rendering):

| Marker Type | Color | Meaning |
|---|---|---|
| `danger` | Red/Amber | Hazard, violence, area to avoid |
| `police` | Amber/Orange | Police presence, checkpoint |
| `safe` | Green | Safe zone, medical post |
| `help` | Blue | Someone needs assistance |
| `gather` | Gray-Blue | Rendezvous point |

### SOS payload format

```json
{
  "type": "sos",
  "lat": 28.6128,
  "lon": 77.2295,
  "status": "ok",
  "timestamp": 1712345678000
}
```

SOS status values: `ok` (safe now), `help` (need assistance), `medical` (need medical aid).

### Marker expiry

- Default TTL: 90 minutes (`expires_at = timestamp + 5400000`).
- Markers older than 90 seconds on receive are ignored (prevents stale markers from traveling the mesh).
- Each phone purges expired markers from local storage every 5 minutes.

## Consequences

- All three payload types share the same mesh envelope and relay pipeline, minimizing code duplication.
- JSON payloads are human-readable for debugging and extensible for future types.
- Marker expiry prevents stale hazard data from misleading users. 90-minute default aligns with typical protest duration needs.
- Config-driven channel list means adding a "Medical" or "Legal" channel is a one-line JSON change, no Kotlin recompilation needed.
- SOS gets its own payload type separate from chat so the receiver can render it with distinct UI treatment (full-screen alert, persistent notification).
