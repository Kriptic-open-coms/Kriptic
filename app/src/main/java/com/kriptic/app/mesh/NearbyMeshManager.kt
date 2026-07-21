package com.kriptic.app.mesh

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NearbyMeshManager(
    private val context: Context,
    private val meshRouter: MeshRouter
) {
    private val serviceId = "com.kriptic.app.mesh.SERVICE_ID"
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    private val connectedEndpoints = mutableMapOf<String, String>() // endpointId -> name

    init {
        meshRouter.registerBroadcastSender { envelope, excludeEndpointId ->
            broadcastEnvelope(envelope, excludeEndpointId)
        }
    }

    fun startMesh(localDeviceName: String) {
        startAdvertising(localDeviceName)
        startDiscovery()
    }

    fun stopMesh() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
        _connectedPeers.value = emptySet()
    }

    private fun startAdvertising(localDeviceName: String) {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startAdvertising(
            localDeviceName,
            serviceId,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d("NearbyMesh", "Advertising started successfully")
        }.addOnFailureListener { e ->
            Log.e("NearbyMesh", "Advertising failed: ${e.message}")
        }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startDiscovery(
            serviceId,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d("NearbyMesh", "Discovery started successfully")
        }.addOnFailureListener { e ->
            Log.e("NearbyMesh", "Discovery failed: ${e.message}")
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d("NearbyMesh", "Endpoint found: $endpointId (${info.endpointName})")
            connectionsClient.requestConnection(
                "KripticUser",
                endpointId,
                connectionLifecycleCallback
            )
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("NearbyMesh", "Endpoint lost: $endpointId")
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d("NearbyMesh", "Accepting connection from $endpointId")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                Log.d("NearbyMesh", "Connected to $endpointId")
                connectedEndpoints[endpointId] = endpointId
                _connectedPeers.value = connectedEndpoints.keys.toSet()
            } else {
                Log.e("NearbyMesh", "Connection failed to $endpointId")
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d("NearbyMesh", "Disconnected from $endpointId")
            connectedEndpoints.remove(endpointId)
            _connectedPeers.value = connectedEndpoints.keys.toSet()
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            try {
                val jsonString = String(bytes, Charsets.UTF_8)
                val envelope = Json.decodeFromString<MessageEnvelope>(jsonString)
                scope.launch {
                    meshRouter.processIncomingMessage(envelope, fromEndpointId = endpointId)
                }
            } catch (e: Exception) {
                Log.e("NearbyMesh", "Failed to decode payload from $endpointId", e)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun broadcastEnvelope(envelope: MessageEnvelope, excludeEndpointId: String? = null) {
        val targets = connectedEndpoints.keys.filter { it != excludeEndpointId }
        if (targets.isEmpty()) return

        val jsonString = Json.encodeToString(envelope)
        val payload = Payload.fromBytes(jsonString.toByteArray(Charsets.UTF_8))
        connectionsClient.sendPayload(targets, payload)
    }
}
