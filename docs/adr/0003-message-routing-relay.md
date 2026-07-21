# ADR 0003: Message Routing & Relay Protocol

## Status

Accepted

## Context

In a BLE mesh where not every phone can talk directly to every other phone (range is ~50-100 meters), some messages must hop through intermediate devices. The protocol needs rules for: how many hops a message can take, how to prevent infinite message loops, how to handle acknowledgements, and how to prioritize urgent messages (SOS, danger alerts) over normal chat.

Three relay strategies were considered:

1. **Flooding** — every phone that receives a message rebroadcasts it to all neighbors. Simple and resilient, but wastes bandwidth and battery since messages propagate to every corner of the mesh even if nobody there needs them.

2. **Directed routing** — the message knows the intended recipient's public key and intermediate phones forward only toward that peer. Efficient but requires every phone to maintain a routing table of who-is-where, which is fragile in a mobile mesh where peers move constantly.

3. **Controlled flooding (flooding with TTL + dedup)** — every phone rebroadcasts, but only if the message has hops remaining (TTL) and only if they haven't seen it before (dedup). No routing tables needed, but limited-scope flooding prevents infinite spread.

## Decision

Use **controlled flooding with TTL, dedup, and priority queuing**.

### Message envelope header (appended by mesh layer, before encryption)

- `message_id` — SHA-256 hash of (senderPubKey + timestamp + payload_type) for global uniqueness. Used for dedup.
- `sender_pub_key` — The originating phone's public key.
- `ttl` — Starts at 7 (max 7 hops, approx 350-700m effective range). Decremented by 1 at each relay. Message dropped when TTL reaches 0.
- `hop_count` — Starts at 0, incremented at each relay. Receiver can estimate distance from source.
- `priority` — 0=normal, 1=priority, 2=SOS. Higher priority messages jump ahead in the relay queue.
- `timestamp` — Unix millis. Messages older than 30 seconds are dropped on arrival (prevents stale relay).
- `signature` — Ed25519 signature over the entire encrypted payload.

### Dedup rules

- Each phone maintains a **bloom filter** of seen message IDs.
- Bloom filter stores up to 10,000 entries (approximately 1 hour of active mesh traffic at 3 msg/sec).
- Filter is cleared when panic wipe triggers.
- If message_id is already in filter, drop silently. No re-forward.

### Relay behavior

1. Phone A sends message → broadcasts to all connected peers.
2. Phone B receives it → checks dedup + TTL + timestamp → if valid, decrements TTL, increments hop_count, adds to relay queue.
3. Relay queue processes highest-priority messages first (SOS always first, then priority, then normal).
4. Phone B broadcasts to its own connected peers.
5. Phone C receives from B → same dedup/TTL check → propagates further if valid.

### Ack semantics

- SOS and priority messages require a **link-layer acknowledgement** (BLE GATT-level). If no ack received within 2 seconds, retransmit up to 3 times.
- Normal chat messages are fire-and-forget. No acknowledgement. The mesh is best-effort for non-critical messages.

## Consequences

- 7-hop TTL gives an effective range of ~350-700m in dense urban environments. Good enough for a protest crowd covering multiple blocks.
- Dedup via bloom filter is memory-efficient (10KB for 10,000 entries) and fast (O(1) lookup).
- No routing tables means no convergence delay when peers move — topology changes are handled instantly.
- SOS messages get priority queue treatment and link-layer acks, ensuring near-100% delivery within the mesh.
- Bloom filter has a tiny false-positive rate (messages dropped that shouldn't be). With 10,000 entries and optimal filter size, false positive rate is ~0.1%. Acceptable trade-off for v1.
- Timestamp-based stale message dropping protects the mesh from old messages circulating indefinitely.
