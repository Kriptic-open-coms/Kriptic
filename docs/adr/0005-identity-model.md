# ADR 0005: Identity Model

## Status

Accepted

## Context

Kriptic needs users to identify each other in the mesh without requiring phone numbers, email addresses, accounts, or any internet-based identity system. The identity must be: unique within a mesh session, persistent across app restarts, cryptographically verifiable, and impossible to change once set (to prevent impersonation).

## Decision

### Identity creation (first launch)

On first app launch:
1. Phone generates an **X25519 keypair** (for encryption) and an **Ed25519 keypair** (for signing) inside Android Keystore (hardware-backed where available).
2. User picks a **username** (3-20 alphanumeric characters).
3. Username is **signed** by the Ed25519 private key, producing a signed identity blob.
4. Identity is stored in encrypted Keystore + encrypted Room database.

### Identity presentation

When the app communicates with peers, it presents:
- `public_key` (X25519, 32 bytes)
- `username` (human-readable label)
- `signature` (Ed25519 signature of public_key + username)

Receivers verify the signature against the public key to confirm the username wasn't tampered with.

### Immutability

- Once set, username and keypair **cannot be changed**. No settings screen for it.
- The only way to get a new identity is to reinstall the app or trigger panic wipe (which destroys the Keystore entry, forcing regeneration on next launch).
- Rationale: prevents identity churn that would break peer reputation and trust within a mesh session.

### Uniqueness guarantee

- No global uniqueness. No central registry, no server.
- Within a mesh session, uniqueness is enforced by collision detection: when two peers have the same username hash, both are alerted and asked to pick a derivative (e.g., "Alex2").
- This constraint is stated plainly in the onboarding UI so users understand the limits.

## Consequences

- No phone number, email, or account required. Compatible with the offline-first principle.
- Android Keystore provides hardware-backed key storage on most modern devices (Android 9+).
- Ed25519 signatures on usernames prevent trivial spoofing (phone B claiming it's phone A).
- Immutability is a deliberate trade-off: prevents identity abuse but means a typo in username is permanent without reinstall.
- No global uniqueness is acceptable for v1 deployment context (protest crowd), where the mesh typically stays under 500 devices and collision probability is low.
