# Map data sources

Record every OSM extract used to build `delhi_ncr.pmtiles`, so the bundle in the app is always traceable back to a specific, reproducible source.

| Date built | OSM extract source (URL) | Extract snapshot date | Tool + version used | Notes |
|---|---|---|---|---|
| 2026-07-23 | [BBBike NewDelhi PBF](https://download.bbbike.org/osm/bbbike/NewDelhi/NewDelhi.osm.pbf) | BBBike file dated 2026-07-23 (local mtime); OSM replication time reported by Planetiler: 2026-07-22T23:00:00Z | Planetiler `ghcr.io/onthegomap/planetiler:latest` (build 0.10.3-SNAPSHOT / githash `32bb493`); OpenMapTiles profile 3.16.0; layers: `transportation,transportation_name,poi,place,building,water,waterway,landuse,landcover,park` | Input sha256 `d03d87d95098b72bea34d2187d1c13c327e6c8ab8c22c4ca701fccdd913fc259` (`input/NewDelhi.osm.pbf`, ~53MB). Output sha256 `47810b63e848196018ca88661d74da2cd97ca063d51eb1351bf1ac79c3cc104a` (`out/delhi_ncr.pmtiles`, ~28MB). Bounds ≈ 76.86–77.75°E, 28.33–28.95°N. Tilemaker bake-off rejected: streets ok, shop/place labels insufficient for Kriptic. |

## How to refresh the Delhi extract

1. Download **Protocolbuffer (PBF)** from BBBike NewDelhi (not Garmin / MBTiles / SVG).
2. Save as `map-data/input/NewDelhi.osm.pbf`.
3. From `map-data/`: `./scripts/build_tiles.sh`
4. Spot-check `out/delhi_ncr.pmtiles` on https://pmtiles.io/
5. Add a new row to the table above with the new checksums/dates.

## ODbL attribution requirement

Because this data is sourced from OpenStreetMap, the app must display OSM's required attribution somewhere reachable (Settings/About screen at minimum). See `docs/06_THIRD_PARTY.md` for what that attribution text needs to say, and keep it current if the wording requirement changes.
