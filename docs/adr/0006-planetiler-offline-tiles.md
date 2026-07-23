# ADR 0006: Planetiler + city PBF extracts for offline maps

## Status
Accepted (team agreement 2026-07-23; bake-off on Delhi / Northern Zone)

## Context

Kriptic needs a fully offline basemap (streets, landmarks, shop names) bundled for Delhi in v1, with a path to additional cities later as optional downloads. Docs left Planetiler vs Tilemaker open (`map-data/README.md`).

Bake-off findings:

- **Tilemaker:** fast, small outputs; streets rendered; shop/place labels missing or not usable in the viewer for our check.
- **Planetiler (OpenMapTiles):** streets + shop labels present; heavier helper downloads and slower setup, acceptable because maps are rebuilt rarely.

Whole-India (or Northern Zone) as a single shipped tileset is too large for an offline protest APK. City/metro packs match the use case.

## Decision

1. Use **Planetiler** with the OpenMapTiles profile to produce **PMTiles**.
2. Source geography from **ready-made city PBF extracts** (v1: BBBike NewDelhi), not DIY clipping of India-scale files.
3. **Prune layers** to: `transportation`, `transportation_name`, `poi`, `place`, `building`, `water`, `waterway`, `landuse`, `landcover`, `park`.
4. **Ship Delhi** in the APK (`app/src/main/assets/map/delhi_ncr.pmtiles` via Git LFS). Additional cities may be offered later as optional in-app downloads when the device has connectivity — never required for core offline Delhi use.
5. Keep a reproducible build script and provenance table under `map-data/` (`scripts/build_tiles.sh`, `sources.md`).

## Consequences

- MapLibre styles should assume OpenMapTiles layer names for the kept layers.
- Rebuilds need Docker + disk for Planetiler helper sources (~1.4GB cached under `map-data/planetiler-sources/`).
- ODbL attribution for OSM must remain visible in-app (see `docs/06_THIRD_PARTY.md`).
- Expanding beyond Delhi is a packaging/catalog problem (more `.pmtiles` files), not a new tile-format decision.
