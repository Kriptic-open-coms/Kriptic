#!/usr/bin/env bash
# Build delhi_ncr.pmtiles from a Delhi OSM extract using Planetiler.
#
# Prerequisites:
#   - Docker
#   - input OSM PBF at map-data/input/NewDelhi.osm.pbf
#     (BBBike Protocolbuffer / PBF extract — see sources.md)
#   - First run downloads ~1.4GB of Planetiler helper data into
#     map-data/planetiler-sources/ (cached for later runs)
#
# Usage (from map-data/):
#   ./scripts/build_tiles.sh
#
# Optional env:
#   OSM_PBF=path/to/file.osm.pbf   # override input
#   JAVA_XMX=1536m                 # Planetiler heap (default 1536m)
#   SKIP_COPY_TO_APP=1             # do not copy into app assets
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "$ROOT/.." && pwd)"
cd "$ROOT"

OSM_PBF="${OSM_PBF:-$ROOT/input/NewDelhi.osm.pbf}"
OUT_PMTILES="$ROOT/out/delhi_ncr.pmtiles"
SOURCES_DIR="$ROOT/planetiler-sources"
LOG_DIR="$ROOT/logs"
APP_ASSETS_MAP="$REPO_ROOT/app/src/main/assets/map"
JAVA_XMX="${JAVA_XMX:-1536m}"
PLANETILER_IMAGE="${PLANETILER_IMAGE:-ghcr.io/onthegomap/planetiler:latest}"

# Layers needed for protest situational awareness (streets + shop/place labels).
ONLY_LAYERS="transportation,transportation_name,poi,place,building,water,waterway,landuse,landcover,park"

if ! command -v docker >/dev/null 2>&1; then
  echo "error: docker is required" >&2
  exit 1
fi

if [[ ! -f "$OSM_PBF" ]]; then
  echo "error: OSM extract not found: $OSM_PBF" >&2
  echo "Download BBBike NewDelhi as Protocolbuffer (PBF) into map-data/input/" >&2
  echo "See map-data/sources.md" >&2
  exit 1
fi

mkdir -p "$SOURCES_DIR" "$LOG_DIR" "$ROOT/out" "$ROOT/data/tmp" "$ROOT/input"

# Docker only mounts map-data/; ensure the PBF is visible under input/.
OSM_BASENAME="$(basename "$OSM_PBF")"
OSM_IN_WORK="$ROOT/input/$OSM_BASENAME"
if [[ "$(cd "$(dirname "$OSM_PBF")" && pwd)/$OSM_BASENAME" != "$OSM_IN_WORK" ]]; then
  cp -f "$OSM_PBF" "$OSM_IN_WORK"
fi

echo "==> Building $OUT_PMTILES"
echo "    input:  $OSM_IN_WORK"
echo "    layers: $ONLY_LAYERS"
echo "    image:  $PLANETILER_IMAGE"
echo "    heap:   $JAVA_XMX"

/usr/bin/time -f 'elapsed_sec=%e' docker run --rm \
  -e JAVA_TOOL_OPTIONS="-Xmx${JAVA_XMX}" \
  -v "$ROOT":/work \
  -w /work \
  "$PLANETILER_IMAGE" \
  --osm-path="/work/input/$OSM_BASENAME" \
  --output=/work/out/delhi_ncr.pmtiles \
  --download \
  --download-dir=/work/planetiler-sources \
  --only-layers="$ONLY_LAYERS" \
  2>&1 | tee "$LOG_DIR/planetiler-delhi.log"

# Docker may leave root-owned outputs; fix when we can.
if docker run --rm -v "$ROOT":/w alpine:3.20 \
  chown -R "$(id -u):$(id -g)" /w/out /w/planetiler-sources /w/logs /w/data 2>/dev/null; then
  :
fi

if [[ ! -f "$OUT_PMTILES" ]]; then
  echo "error: expected output missing: $OUT_PMTILES" >&2
  exit 1
fi

ls -lh "$OUT_PMTILES"

if [[ "${SKIP_COPY_TO_APP:-}" != "1" ]]; then
  mkdir -p "$APP_ASSETS_MAP"
  cp -f "$OUT_PMTILES" "$APP_ASSETS_MAP/delhi_ncr.pmtiles"
  echo "==> Copied to $APP_ASSETS_MAP/delhi_ncr.pmtiles"
  echo "    Commit that file via Git LFS (see repo .gitattributes)."
fi

echo "==> Done. Spot-check on https://pmtiles.io/ before relying on it."
echo "    Record/update the build row in map-data/sources.md."
