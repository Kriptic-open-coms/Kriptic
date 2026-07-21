package com.kriptic.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kriptic.app.calculator.CalculatorScreen
import com.kriptic.app.map.HazardBroadcastBridge
import com.kriptic.app.map.MapScreen
import com.kriptic.app.mesh.CryptoService
import com.kriptic.app.mesh.MeshRouter
import com.kriptic.app.mesh.MessageType
import com.kriptic.app.mesh.NearbyMeshManager
import com.kriptic.app.reference.ReferenceArticle
import com.kriptic.app.reference.ReferenceDetailScreen
import com.kriptic.app.reference.ReferenceRepository
import com.kriptic.app.reference.ReferenceSearchScreen
import com.kriptic.app.security.PanicWipeManager
import com.kriptic.app.security.StealthModeController
import com.kriptic.app.sos.SosButtonReceiver
import com.kriptic.app.ui.chat.ChatScreen
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.Danger
import com.kriptic.app.ui.theme.DarkBackground
import com.kriptic.app.ui.theme.DarkBorder
import com.kriptic.app.ui.theme.DarkSurface
import com.kriptic.app.ui.theme.DarkSurfaceElevated
import com.kriptic.app.ui.theme.DarkTextPrimary
import com.kriptic.app.ui.theme.DarkTextSecondary
import com.kriptic.app.ui.theme.Heading
import com.kriptic.app.ui.theme.Title
import kotlinx.coroutines.launch

enum class KripticTab {
    CHAT, MAP, REFERENCE, SECURITY
}

@Composable
fun KripticMainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Core Engine Singletons
    val meshRouter = remember { MeshRouter() }
    val cryptoService = remember { CryptoService(context) }
    val meshManager = remember { NearbyMeshManager(context, meshRouter) }
    val hazardBridge = remember { HazardBroadcastBridge(meshRouter, cryptoService) }
    val sosReceiver = remember { SosButtonReceiver(meshRouter, cryptoService) }
    val panicWipeManager = remember { PanicWipeManager(meshRouter) }
    val stealthController = remember { StealthModeController(context) }
    val referenceRepository = remember { ReferenceRepository(context) }

    // State Variables
    var activeTab by remember { mutableStateOf(KripticTab.CHAT) }
    var isStealthDisguiseActive by remember { mutableStateOf(false) }
    var selectedArticleDetail by remember { mutableStateOf<ReferenceArticle?>(null) }
    val connectedPeers by meshManager.connectedPeers.collectAsState()

    // Auto-start Mesh Advertising & Discovery
    DisposableEffect(Unit) {
        meshManager.startMesh(localDeviceName = "Device_${cryptoService.getPublicKeyBase64().take(4)}")
        onDispose {
            meshManager.stopMesh()
        }
    }

    // Listen for hazard pin bridge events
    androidx.compose.runtime.LaunchedEffect(Unit) {
        scope.launch { hazardBridge.listenForHazardBroadcasts() }
    }

    // If stealth calculator launcher is active
    if (isStealthDisguiseActive) {
        CalculatorScreen(
            secretPin = "1337",
            onUnlockSecretApp = {
                isStealthDisguiseActive = false
                Toast.makeText(context, "Kriptic Unlocked", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // Article detail screen view override
    if (selectedArticleDetail != null) {
        ReferenceDetailScreen(
            article = selectedArticleDetail!!,
            onBack = { selectedArticleDetail = null }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == KripticTab.CHAT,
                    onClick = { activeTab = KripticTab.CHAT },
                    label = { Text("Mesh Chat", style = Caption) },
                    icon = { Text("💬", style = Heading) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Accent,
                        selectedTextColor = Accent,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )

                NavigationBarItem(
                    selected = activeTab == KripticTab.MAP,
                    onClick = { activeTab = KripticTab.MAP },
                    label = { Text("Delhi Map", style = Caption) },
                    icon = { Text("🗺️", style = Heading) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Accent,
                        selectedTextColor = Accent,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )

                NavigationBarItem(
                    selected = activeTab == KripticTab.REFERENCE,
                    onClick = { activeTab = KripticTab.REFERENCE },
                    label = { Text("Rights & Aid", style = Caption) },
                    icon = { Text("📚", style = Heading) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Accent,
                        selectedTextColor = Accent,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )

                NavigationBarItem(
                    selected = activeTab == KripticTab.SECURITY,
                    onClick = { activeTab = KripticTab.SECURITY },
                    label = { Text("Security", style = Caption) },
                    icon = { Text("🛡️", style = Heading) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Accent,
                        selectedTextColor = Accent,
                        unselectedIconColor = DarkTextSecondary,
                        unselectedTextColor = DarkTextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (activeTab) {
                KripticTab.CHAT -> {
                    ChatScreen(
                        meshRouter = meshRouter,
                        cryptoService = cryptoService,
                        connectedPeersCount = connectedPeers.size,
                        onSendMessage = { text ->
                            val encrypted = cryptoService.encryptPayload(text)
                            val envelope = com.kriptic.app.mesh.MessageEnvelope(
                                senderPubKey = cryptoService.getPublicKeyBase64(),
                                payloadEncrypted = encrypted,
                                type = MessageType.CHAT
                            )
                            meshManager.broadcastEnvelope(envelope)
                            scope.launch {
                                meshRouter.processIncomingMessage(envelope)
                            }
                        }
                    )
                }
                KripticTab.MAP -> {
                    MapScreen(
                        hazardBridge = hazardBridge,
                        onDropPin = { newPin ->
                            hazardBridge.broadcastNewHazardPin(newPin)
                        }
                    )
                }
                KripticTab.REFERENCE -> {
                    ReferenceSearchScreen(
                        repository = referenceRepository,
                        onSelectArticle = { article ->
                            selectedArticleDetail = article
                        }
                    )
                }
                KripticTab.SECURITY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(text = "Security & Stealth Suite", style = Title, color = DarkTextPrimary)
                        Text(text = "Emergency panic destruction & launcher disguise controls", style = Caption, color = DarkTextSecondary)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Silent SOS Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(text = "🚨 Silent SOS Trigger", style = Heading, color = Danger)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Broadcasts your location & emergency status to all mesh peers. Triggerable via Volume-Down button x3.", style = Caption, color = DarkTextSecondary)

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        sosReceiver.triggerSilentSos()
                                        Toast.makeText(context, "Silent SOS Broadcast Sent over Mesh!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Broadcast Silent SOS", style = Body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stealth Launcher Calculator Mode Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(text = "🧮 Calculator Launcher Disguise", style = Heading, color = DarkTextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Disguises the app icon on the Android home screen as a working calculator. Enter passcode 1337 + = to unlock.", style = Caption, color = DarkTextSecondary)

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        stealthController.enableStealthCalculatorMode()
                                        isStealthDisguiseActive = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Lock App into Calculator Mode", style = Body, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Panic Wipe Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(text = "⚠️ Emergency Panic Wipe", style = Heading, color = Danger)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Instantly wipes local database, identity keypair from KeyStore, and cached mesh history.", style = Caption, color = DarkTextSecondary)

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        panicWipeManager.executePanicWipe(context) {
                                            Toast.makeText(context, "All Local Data Wiped!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313136)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Wipe All Local Data Immediately", style = Body, color = Danger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
