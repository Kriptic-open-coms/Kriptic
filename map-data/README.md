# map-data

Build pipeline that produces `app/src/main/assets/map/delhi_ncr.pmtiles`, the offline vector tile bundle shipped inside the APK.

This directory does not ship inside the app itself — it's the tooling that produces the file that does.

## Pipeline (planned)

1. Pull an OpenStreetMap extract covering Delhi NCR (source URL and extract date recorded in `sources.md` for reproducibility — OSM extracts go stale, so we need to know exactly which snapshot is in the app at any time).
2. Run `planetiler` (or `tilemaker`, TBD — see open question below) to build a PMTiles bundle, including landmark, POI, and shop-name layers, not just the road network.
3. Verify the output renders correctly in MapLibre before committing.
4. Commit the resulting `.pmtiles` file via **Git LFS** (it's a binary blob, don't commit it directly to Git history) and copy/symlink it into `app/src/main/assets/map/`.

## Open question: planetiler vs. tilemaker

Both are viable OSM-to-vector-tiles tools. Whoever picks this up should do a quick bake-off on the actual Delhi NCR extract size/render quality/build time before committing, and record the decision as an ADR in `docs/adr/`.

## `sources.md`

Track exactly which OSM extract (source, date, URL/checksum) produced the currently-committed tile bundle, so we can reproduce or update it later without guessing.

## Regenerating the bundle

```bash
./scripts/build_tiles.sh   # placeholder — fill in once the pipeline is implemented
```
