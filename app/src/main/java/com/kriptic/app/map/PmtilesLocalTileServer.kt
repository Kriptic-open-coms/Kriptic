package com.kriptic.app.map

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * MapLibre Android's vanilla SDK doesn't understand a `pmtiles://` source
 * URL — that requires either a custom protocol resolver plugin or (the
 * approach used here) serving the tiles over ordinary HTTP so a standard
 * `"tiles": ["http://127.0.0.1:<port>/{z}/{x}/{y}.pbf"]` vector source
 * works with zero MapLibre-side customization.
 *
 * This starts a tiny loopback-only HTTP server (no external interface,
 * `InetAddress.getLoopbackAddress()` only — never reachable off-device)
 * on first use, copies the bundled asset to internal storage once (Android
 * assets aren't directly seekable/random-access, which PmtilesReader
 * needs), and serves tile bytes straight out of the pmtiles file via
 * [PmtilesReader].
 *
 * Uses a fixed port rather than an ephemeral one so a static style JSON
 * (delhi_ncr_style.json) can reference the URL without needing to be
 * generated at runtime. [start] is idempotent — safe to call from
 * multiple Compose recompositions.
 */
object PmtilesLocalTileServer {
    private const val TAG = "PmtilesLocalTileServer"
    const val PORT = 8017
    val baseUrl = "http://127.0.0.1:$PORT"

    @Volatile private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    private val pathPattern = Pattern.compile("^/(\\d+)/(\\d+)/(\\d+)\\.pbf$")

    @Synchronized
    fun start(context: Context, assetPath: String = "map/delhi_ncr_sample.pmtiles") {
        if (serverSocket != null) return

        val localFile = copyAssetIfNeeded(context, assetPath)
        val raf = RandomAccessFile(localFile, "r")
        val reader = PmtilesReader(raf)

        val socket = ServerSocket(PORT, 50, InetAddress.getLoopbackAddress())
        serverSocket = socket

        executor.execute {
            while (true) {
                val client = try {
                    socket.accept()
                } catch (e: Exception) {
                    break // socket closed
                }
                executor.execute { handleClient(client, reader) }
            }
        }
    }

    fun stop() {
        serverSocket?.close()
        serverSocket = null
    }

    private fun copyAssetIfNeeded(context: Context, assetPath: String): File {
        val outDir = File(context.filesDir, "map").apply { mkdirs() }
        val outFile = File(outDir, File(assetPath).name)
        val assetSize = context.assets.open(assetPath).use { it.available() }
        if (!outFile.exists() || outFile.length() != assetSize.toLong()) {
            context.assets.open(assetPath).use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return outFile
    }

    private fun handleClient(client: Socket, reader: PmtilesReader) {
        client.use { sock ->
            try {
                val requestLine = sock.getInputStream().bufferedReader().readLine() ?: return
                // e.g. "GET /10/739/406.pbf HTTP/1.1"
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                val path = parts[1]
                val matcher = pathPattern.matcher(path)
                val output = sock.getOutputStream()

                if (!matcher.matches()) {
                    output.write("HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n".toByteArray())
                    return
                }
                val z = matcher.group(1)!!.toInt()
                val x = matcher.group(2)!!.toInt()
                val y = matcher.group(3)!!.toInt()

                val tileBytes = try {
                    reader.getTile(z, x, y)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading tile $z/$x/$y: ${e.message}")
                    null
                }

                if (tileBytes == null) {
                    // 204 (not 404) — MapLibre treats an empty tile as
                    // "nothing here", not a broken source, which is the
                    // correct behavior for gaps in a sparse test dataset.
                    output.write("HTTP/1.1 204 No Content\r\nContent-Length: 0\r\n\r\n".toByteArray())
                } else {
                    val header = buildString {
                        append("HTTP/1.1 200 OK\r\n")
                        append("Content-Type: application/x-protobuf\r\n")
                        append("Content-Encoding: gzip\r\n") // tiles were written gzip-compressed
                        append("Content-Length: ${tileBytes.size}\r\n")
                        append("Connection: close\r\n\r\n")
                    }
                    output.write(header.toByteArray())
                    output.write(tileBytes)
                }
                output.flush()
            } catch (e: Exception) {
                Log.w(TAG, "Client handling error: ${e.message}")
            }
        }
    }
}