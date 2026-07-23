package com.kriptic.app.map

import java.io.ByteArrayInputStream
import java.io.RandomAccessFile
import java.util.zip.GZIPInputStream

/**
 * Minimal PMTiles v3 reader, ported field-for-field from the reference
 * Python implementation at https://github.com/protomaps/PMTiles
 * (`pmtiles/tile.py` + `pmtiles/reader.py`) — this project's own
 * `gen_sample_pmtiles.py` uses that same Python library to write the
 * bundled sample file, so this reader was validated against tiles that
 * library actually produced (verified during development: header fields
 * round-tripped correctly, directory entries resolved to the expected
 * tile bytes). It has NOT been tested against pmtiles files produced by
 * other tools (planetiler, tippecanoe, go-pmtiles) — those should work
 * per spec, but re-verify once a real file exists.
 *
 * Only implements reading (no writing) and does not implement the
 * `leaf_directory` recursion beyond depth — i.e. datasets large enough to
 * need multiple directory levels. The bundled sample fits in a single
 * root directory (125 entries, no leaf dirs) so this wasn't exercised;
 * the recursion is implemented per spec anyway since a real Delhi NCR
 * build (many more tiles) will very likely need it.
 */
class PmtilesReader(private val file: RandomAccessFile) {

    data class Header(
        val rootOffset: Long,
        val rootLength: Long,
        val metadataOffset: Long,
        val metadataLength: Long,
        val leafDirectoryOffset: Long,
        val leafDirectoryLength: Long,
        val tileDataOffset: Long,
        val tileDataLength: Long,
        val internalCompression: Int, // 1=none, 2=gzip
        val tileCompression: Int,
        val tileType: Int,
        val minZoom: Int,
        val maxZoom: Int,
    )

    private data class Entry(val tileId: Long, var offset: Long, var length: Long, var runLength: Long)

    private fun readBytes(offset: Long, length: Int): ByteArray {
        val buf = ByteArray(length)
        file.seek(offset)
        file.readFully(buf)
        return buf
    }

    private fun u64(buf: ByteArray, pos: Int): Long {
        var result = 0L
        for (i in 0 until 8) result = result or ((buf[pos + i].toLong() and 0xFF) shl (8 * i))
        return result
    }

    private fun i32(buf: ByteArray, pos: Int): Int {
        var result = 0
        for (i in 0 until 4) result = result or ((buf[pos + i].toInt() and 0xFF) shl (8 * i))
        return result
    }

    fun header(): Header {
        val buf = readBytes(0, 127)
        require(String(buf, 0, 7, Charsets.US_ASCII) == "PMTiles") { "Not a PMTiles file (bad magic)" }
        require(buf[7].toInt() == 3) { "Unsupported PMTiles spec version: ${buf[7]}" }
        return Header(
            rootOffset = u64(buf, 8),
            rootLength = u64(buf, 16),
            metadataOffset = u64(buf, 24),
            metadataLength = u64(buf, 32),
            leafDirectoryOffset = u64(buf, 40),
            leafDirectoryLength = u64(buf, 48),
            tileDataOffset = u64(buf, 56),
            tileDataLength = u64(buf, 64),
            internalCompression = buf[97].toInt() and 0xFF,
            tileCompression = buf[98].toInt() and 0xFF,
            tileType = buf[99].toInt() and 0xFF,
            minZoom = buf[100].toInt() and 0xFF,
            maxZoom = buf[101].toInt() and 0xFF,
        )
    }

    private fun readVarint(stream: ByteArrayInputStream): Long {
        var shift = 0
        var result = 0L
        while (true) {
            val b = stream.read()
            if (b == -1) throw IllegalStateException("unexpected end of varint stream")
            result = result or ((b.toLong() and 0x7F) shl shift)
            shift += 7
            if (b and 0x80 == 0) break
        }
        return result
    }

    private fun decompress(bytes: ByteArray, compression: Int): ByteArray = when (compression) {
        2 -> GZIPInputStream(ByteArrayInputStream(bytes)).use { it.readBytes() }
        else -> bytes // 1 = none; other schemes (brotli/zstd) unsupported here
    }

    private fun deserializeDirectory(raw: ByteArray, header: Header): List<Entry> {
        val decompressed = decompress(raw, header.internalCompression)
        val stream = ByteArrayInputStream(decompressed)
        val numEntries = readVarint(stream).toInt()

        val entries = ArrayList<Entry>(numEntries)
        var lastId = 0L
        for (i in 0 until numEntries) {
            val delta = readVarint(stream)
            lastId += delta
            entries.add(Entry(tileId = lastId, offset = 0, length = 0, runLength = 0))
        }
        for (i in 0 until numEntries) entries[i].runLength = readVarint(stream)
        for (i in 0 until numEntries) entries[i].length = readVarint(stream)
        for (i in 0 until numEntries) {
            val tmp = readVarint(stream)
            entries[i].offset = if (i > 0 && tmp == 0L) {
                entries[i - 1].offset + entries[i - 1].length
            } else {
                tmp - 1
            }
        }
        return entries
    }

    /** Binary search matching the reference `find_tile` (handles run-length ranges). */
    private fun findTile(entries: List<Entry>, tileId: Long): Entry? {
        var m = 0
        var n = entries.size - 1
        while (m <= n) {
            val k = (n + m) ushr 1
            val c = tileId - entries[k].tileId
            when {
                c > 0 -> m = k + 1
                c < 0 -> n = k - 1
                else -> return entries[k]
            }
        }
        if (n >= 0) {
            val candidate = entries[n]
            if (candidate.runLength == 0L) return candidate
            if (tileId - candidate.tileId < candidate.runLength) return candidate
        }
        return null
    }

    /** Returns the raw (still tile-compressed, e.g. gzip) tile bytes, or null if absent. */
    fun getTile(z: Int, x: Int, y: Int): ByteArray? {
        val tileId = zxyToTileId(z, x, y)
        val header = header()
        var dirOffset = header.rootOffset
        var dirLength = header.rootLength
        repeat(4) { // max directory depth per spec
            val directory = deserializeDirectory(readBytes(dirOffset, dirLength.toInt()), header)
            val result = findTile(directory, tileId) ?: return null
            if (result.runLength == 0L) {
                dirOffset = header.leafDirectoryOffset + result.offset
                dirLength = result.length
            } else {
                return readBytes(header.tileDataOffset + result.offset, result.length.toInt())
            }
        }
        return null
    }

    companion object {
        /** Hilbert-curve zxy -> tile_id, ported from pmtiles/tile.py. */
        fun zxyToTileId(z: Int, x0: Int, y0: Int): Long {
            var x = x0
            var y = y0
            var acc = ((1L shl (z * 2)) - 1) / 3
            var a = z - 1
            while (a >= 0) {
                val s = 1 shl a
                val rx = s and x
                val ry = s and y
                acc += ((3L * rx) xor ry.toLong()) shl a
                // rotate
                if (ry == 0) {
                    if (rx != 0) {
                        x = s - 1 - x
                        y = s - 1 - y
                    }
                    val tmp = x
                    x = y
                    y = tmp
                }
                a -= 1
            }
            return acc
        }
    }
}
