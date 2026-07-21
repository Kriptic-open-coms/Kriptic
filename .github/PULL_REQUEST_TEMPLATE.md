## What this changes

<!-- Short description. Link the tracking issue if there is one. -->

## Area touched

- [ ] `mesh/` (safety-critical — needs 2 reviews)
- [ ] `map/` or `map-data/`
- [ ] `sos/` or `security/` (safety-critical — needs 2 reviews)
- [ ] `knowledge/` or `content/` (needs subject-matter review if `content/legal/` or `content/firstaid/` changed — add `content-review-needed` label)
- [ ] UI / design system
- [ ] Docs only

## How was this tested?

<!-- For mesh/map/sos/security changes: real-device testing is required, not optional. -->
- [ ] Tested on 2+ physical Android devices in airplane mode
- [ ] Unit tests added/updated for logic touched (TTL/dedup, marker expiry, etc.)
- [ ] N/A — docs/content only

## Checklist

- [ ] I checked this against `docs/03_SCOPE.md` — it's in scope for the current milestone, or I flagged it as a roadmap proposal instead
- [ ] No hardcoded colors/fonts outside `ui/theme/DesignTokens.kt`
- [ ] I updated relevant docs if this changes architecture, scope, or structure
