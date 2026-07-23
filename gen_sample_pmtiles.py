import math
import gzip
import mapbox_vector_tile
from pmtiles.writer import Writer
from pmtiles.tile import zxy_to_tileid, TileType, Compression

# NCR-ish bbox (matches map-data/README.md)
MINLON, MINLAT, MAXLON, MAXLAT = 76.8, 28.3, 77.7, 28.9

# A handful of clearly-fake "landmark" test points spread across the bbox,
# labeled as TEST so nobody mistakes this for real OSM data.
TEST_POINTS = [
    (77.2090, 28.6139, "TEST — India Gate area"),
    (77.0266, 28.4595, "TEST — Gurugram area"),
    (77.3910, 28.5355, "TEST — Noida area"),
    (77.3178, 28.6692, "TEST — Ghaziabad area"),
    (77.3178, 28.4089, "TEST — Faridabad area"),
]

def lonlat_to_tile(lon, lat, z):
    lat_rad = math.radians(lat)
    n = 2 ** z
    x = (lon + 180.0) / 360.0 * n
    y = (1.0 - math.log(math.tan(lat_rad) + 1 / math.cos(lat_rad)) / math.pi) / 2.0 * n
    return x, y

def lonlat_to_tilepixel(lon, lat, z, x_tile, y_tile, extent=4096):
    x, y = lonlat_to_tile(lon, lat, z)
    px = int((x - x_tile) * extent)
    py = int((y - y_tile) * extent)
    return px, py

def tiles_covering_bbox(z):
    x0, y0 = lonlat_to_tile(MINLON, MAXLAT, z)  # top-left (max lat)
    x1, y1 = lonlat_to_tile(MAXLON, MINLAT, z)  # bottom-right (min lat)
    xs = range(int(math.floor(x0)), int(math.floor(x1)) + 1)
    ys = range(int(math.floor(y0)), int(math.floor(y1)) + 1)
    return [(x, y) for x in xs for y in ys]

def build_tile_bytes(z, x_tile, y_tile):
    extent = 4096
    points_feats = []
    for lon, lat, label in TEST_POINTS:
        px, py = lonlat_to_tile(lon, lat, z)
        if not (x_tile <= px < x_tile + 1 and y_tile <= py < y_tile + 1):
            continue
        gx = int((px - x_tile) * extent)
        gy = int((py - y_tile) * extent)
        points_feats.append({
            "geometry": {"type": "Point", "coordinates": [gx, gy]},
            "properties": {"name": label, "kind": "sample_landmark"},
        })

    # rough NCR boundary rectangle, in this tile's local pixel space (clipped)
    corners = [
        (MINLON, MINLAT), (MAXLON, MINLAT), (MAXLON, MAXLAT), (MINLON, MAXLAT), (MINLON, MINLAT)
    ]
    ring = []
    for lon, lat in corners:
        px, py = lonlat_to_tile(lon, lat, z)
        gx = int((px - x_tile) * extent)
        gy = int((py - y_tile) * extent)
        ring.append([gx, gy])

    layers = []
    if points_feats:
        layers.append({
            "name": "sample_landmarks",
            "features": points_feats,
        })
    layers.append({
        "name": "sample_boundary",
        "features": [{
            "geometry": {"type": "Polygon", "coordinates": [ring]},
            "properties": {"name": "TEST — NCR sample boundary (NOT real data)"},
        }],
    })

    return mapbox_vector_tile.encode(layers, quantize_bounds=None, extents=extent)

ZOOMS = [6, 8, 10, 12]

with open("/home/claude/work/kriptic-android/app/src/main/assets/map/delhi_ncr_sample.pmtiles", "wb") as f:
    writer = Writer(f)
    min_lon_all, min_lat_all, max_lon_all, max_lat_all = MINLON, MINLAT, MAXLON, MAXLAT
    count = 0
    for z in ZOOMS:
        for (x, y) in tiles_covering_bbox(z):
            data = build_tile_bytes(z, x, y)
            gz = gzip.compress(data)
            tileid = zxy_to_tileid(z, x, y)
            writer.write_tile(tileid, gz)
            count += 1

    header = {
        "tile_type": TileType.MVT,
        "tile_compression": Compression.GZIP,
        "min_zoom": min(ZOOMS),
        "max_zoom": max(ZOOMS),
        "min_lon_e7": int(min_lon_all * 10_000_000),
        "min_lat_e7": int(min_lat_all * 10_000_000),
        "max_lon_e7": int(max_lon_all * 10_000_000),
        "max_lat_e7": int(max_lat_all * 10_000_000),
        "center_zoom": 10,
        "center_lon_e7": int(((min_lon_all + max_lon_all) / 2) * 10_000_000),
        "center_lat_e7": int(((min_lat_all + max_lat_all) / 2) * 10_000_000),
    }
    metadata = {
        "name": "Kriptic sample/test tiles (NOT real map data)",
        "description": "Synthetic placeholder tiles for pipeline testing only. Replace with real Delhi NCR tiles per map-data/README.md before any real use.",
        "format": "pbf",
    }
    writer.finalize(header, metadata)
    print(f"wrote {count} tiles across zooms {ZOOMS}")
