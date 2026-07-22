# Delhi NCR offline map tiles — build pipeline (NOT included in this scaffold)

`MapScreen.kt` expects two files that are **not present** in this repo yet:

```
app/src/main/assets/map/delhi_ncr.pmtiles
app/src/main/assets/map/delhi_ncr_style.json
```

They couldn't be generated in the environment this scaffold was built in —
that environment's network access is restricted to package registries
(npm/pypi/crates/GitHub) and can't reach Geofabrik or download a
multi-hundred-MB OSM extract. This has to be done once, on a machine with
normal internet access, by a human on the team. It's a few real steps, not
a research problem:

## 1. Get the raw OSM data
Download the Delhi NCR extract (or all of India, then clip it) from
Geofabrik: https://download.geofabrik.de/asia/india.html
(India is the smallest official extract that contains NCR — there's no
official Delhi-only extract, so you'll clip it in step 2.)

## 2. Clip to the NCR bounding box
Use `osmium extract` (from `osmium-tool`) with a bounding box roughly
covering Delhi + Gurugram + Noida + Faridabad + Ghaziabad
(~76.8°E–77.7°E, ~28.3°N–28.9°N — adjust to taste):

```
osmium extract -b 76.8,28.3,77.7,28.9 india-latest.osm.pbf -o delhi_ncr.osm.pbf
```

## 3. Build vector tiles
Use `planetiler` (fastest, Java-based, actively maintained) with its
OpenMapTiles profile:

```
java -jar planetiler.jar --area=delhi_ncr --osm-path=delhi_ncr.osm.pbf \
  --output=delhi_ncr.pmtiles
```

This produces the `.pmtiles` file MapScreen.kt loads. Landmarks, shop
names, roads, and building footprints come from whatever's tagged in
OSM for that area — Delhi NCR's OSM coverage is generally good in central
areas, spottier in newer suburban development, so expect some gaps.

## 4. Write the style JSON
`delhi_ncr_style.json` is a standard MapLibre style spec
(https://maplibre.org/maplibre-style-spec/) pointing its `sources` entry
at the bundled pmtiles file via a `pmtiles://` URL, plus whatever
`layers` you want rendered (roads, buildings, labels, POIs). Start from
one of the open OpenMapTiles-compatible starter styles rather than
writing layer definitions from scratch.

## 5. Bundle it
Drop both files into `app/src/main/assets/map/`. `MapScreen.kt` will pick
them up automatically — no code changes needed once they exist.

## Size expectations
A Delhi NCR extract at reasonable detail will likely land somewhere in
the tens-to-low-hundreds of MB range depending on zoom levels included.
Consider Android App Bundle dynamic delivery or an in-app download-on-
first-launch step instead of bundling it directly in the APK if it's
large enough to matter for install size.
