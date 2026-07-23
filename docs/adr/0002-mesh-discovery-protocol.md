# ADR 0002: Mesh Discovery Protocol

## Status

Accepted

## Context

Kriptic's mesh depends on phones finding each other without internet, cell towers, or any centralized infrastructure. When a user opens the app, it needs to discover nearby devices running Kriptic and establish a communication channel — all over Bluetooth Low Energy (BLE).

Three options were considered:

1. **BLE Peripheral/Central mode scanning** — standard BLE where one device advertises and others scan. Simple but asymmetric: a phone can only be one role (advertiser or scanner) at a time. Not ideal for peer-to-peer discovery since two phones might both be scanning and miss each other.

2. **BLE GATT server + client** — each phone runs both a GATT server (advertising services) and a GATT client (scanning for others). This is what bitchat-android already implements. Reliable but requires connection-oriented communication which has latency overhead for discovery.

3. **BLE Advertising packets (connectionless)** — phones broadcast small data payloads in BLE advertisement packets without needing to connect. Other phones pick them up from scan results. Faster discovery, no connection setup overhead, but limited payload size (~31 bytes per packet).

## Decision

Use **BLE Advertising packets for peer discovery** with **GATT for data transfer**.

Phase 1 — Discovery:
- Each phone broadcasts a small **announcement packet** in BLE advertisement data containing:
  - Protocol magic bytes (identifies Kriptic)
  - Public key fingerprint (truncated, 8 bytes)
  - Username hash (4 bytes for quick display resolution)
  - Protocol version (1 byte)

- Announcements broadcast every 5 seconds while app is foregrounded, every 30 seconds while backgrounded, and stop completely when panic wipe is triggered.

Phase 2 — Connection:
- When phone A sees phone B's announcement, phone A initiates a **BLE GATT connection** to phone B.
- Once connected, they exchange full identity information (full public key, username) and establish encrypted session.
- Connection remains open while both are in range (no repeated handshake for each message).

## Consequences

- Discovery is near-instant (advertisement packets picked up within 1-5 seconds) and battery-efficient (5-second interval is standard BLE practice).
- GATT-based data transfer allows arbitrary message sizes, not limited to the 31-byte advertisement payload.
- Being connection-oriented means we have reliable delivery (acknowledgements, retransmit on failure) — important for SOS and danger alerts.
- Phones must implement both central and peripheral BLE roles simultaneously, which not all Android devices support well. Mitigation: fall back to sequential role switching (advertise for 1 second, scan for 4 seconds) detected via BLE scan callback failures.
- The 5-second announcement interval means discovery latency is bounded by ~5 seconds worst case — acceptable for v1.
- Panic wipe stops all BLE activity immediately, ensuring the phone disappears from the mesh.
