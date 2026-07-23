# Delhi NCR offline map tiles

Build pipeline that produces `delhi_ncr.pmtiles`, the offline vector tile bundle shipped inside the APK at `app/src/main/assets/map/`.

This directory is tooling + provenance — the APK consumes the copied `.pmtiles` under `app/`, not these scripts at runtime.

## Decision

- **Builder:** Planetiler (OpenMapTiles profile), not Tilemaker — Tilemaker’s Delhi bake-off lacked usable shop/place labels; Planetiler showed streets + shop labels.
- **Geography:** city/metro extracts (BBBike NewDelhi for v1), not all-India. More cities can be added later as optional in-app downloads when online.
- **Layers:** pruned to what the Maps tab needs — see `scripts/build_tiles.sh` (`ONLY_LAYERS`).
- Recorded in `docs/adr/0006-planetiler-offline-tiles.md`.



## Pipeline

1. Download a Delhi OSM **PBF** extract (BBBike NewDelhi) into `input/NewDelhi.osm.pbf` — URL/checksum in `sources.md`.
2. Run `./scripts/build_tiles.sh` (Docker + Planetiler). First run caches helper data under `planetiler-sources/`.
3. Spot-check `out/delhi_ncr.pmtiles` on [https://pmtiles.io/](https://pmtiles.io/) (streets + shop names).
4. Script copies the file to `app/src/main/assets/map/delhi_ncr.pmtiles` (Git LFS — see repo `.gitattributes`).
5. Update the row in `sources.md` when you rebuild.



```
osmium extract -b 76.8,28.3,77.7,28.9 india-latest.osm.pbf -o delhi_ncr.osm.pbf
```

```bash
cd map-data
# place/refresh input/NewDelhi.osm.pbf first
./scripts/build_tiles.sh
```

Optional:

```bash
JAVA_XMX=2g SKIP_COPY_TO_APP=1 ./scripts/build_tiles.sh
```



## Layout

```text
map-data/
  input/                 # *.osm.pbf extracts (not required in git if re-downloadable)
  out/                   # built *.pmtiles
  planetiler-sources/    # Planetiler helper downloads (cached, gitignored)
  logs/
  scripts/build_tiles.sh
  sources.md
```

