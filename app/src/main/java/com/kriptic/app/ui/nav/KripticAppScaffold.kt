package com.kriptic.app.ui.nav

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kriptic.app.identity.KripticIdentityRepository
import com.kriptic.app.identity.UsernameRegistrationScreen
import com.kriptic.app.knowledge.KnowledgeDetailScreen
import com.kriptic.app.knowledge.KnowledgeRepository
import com.kriptic.app.knowledge.KnowledgeSearchScreen
import com.kriptic.app.map.MapScreen
import com.kriptic.app.map.MarkerBroadcastBridge
import com.kriptic.app.map.MarkerRepository
import com.kriptic.app.map.MarkerType
import com.kriptic.app.ui.ChatScreen
import com.kriptic.app.ui.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Long-press only (no onClick) so a stray tap can never fire an SOS —
 * matches "no confirmation dialog, the trigger is the confirmation" by
 * making the gesture itself deliberately unambiguous instead of adding a
 * dialog on top of a plain tap.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableForSos(onFire: () -> Unit): Modifier =
    this.combinedClickable(onClick = {}, onLongClick = onFire)

private enum class KripticTab(val label: String) {
    MESSAGING("Messaging"),
    MAPS("Maps"),
    KNOWLEDGE("Knowledge"),
}

private sealed class KnowledgeNavState {
    object List : KnowledgeNavState()
    data class Detail(val entryId: String) : KnowledgeNavState()
}

/**
 * Replaces the bare `ChatScreen(viewModel = chatViewModel)` call at
 * MainActivity's OnboardingState.COMPLETE branch. This is the single
 * integration seam tying together everything built in this pass:
 * username gate -> 3-tab bottom nav (Messaging / Maps / Knowledge) per
 * the mockups and docs/04_DESIGN_SYSTEM.md.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KripticAppScaffold(chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    val identityRepository = remember { KripticIdentityRepository(context) }
    var isRegistered by remember { mutableStateOf(identityRepository.isRegistered()) }

    if (!isRegistered) {
        UsernameRegistrationScreen(
            repository = identityRepository,
            existingPeerNicknames = emptyList(), // wire real visible-peer list here once mesh is up
            onRegistered = { username ->
                chatViewModel.setNickname(username)
                isRegistered = true
            },
        )
        return
    }

    val markerRepository = remember { MarkerRepository(context) }
    val knowledgeRepository = remember { KnowledgeRepository(context) }
    val bridgeScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    val markerBridge = remember {
        MarkerBroadcastBridge(chatViewModel.meshServiceFacade, markerRepository, bridgeScope)
    }
    val sosTrigger = remember {
        com.kriptic.app.sos.SosTrigger(
            meshService = chatViewModel.meshServiceFacade,
            getMyPubKeyHex = { chatViewModel.myPeerID },
            getMyNickname = { chatViewModel.nickname.value },
        )
    }
    var activeSos by remember { mutableStateOf<com.kriptic.app.sos.SosPayload?>(null) }

    LaunchedEffect(Unit) {
        // Kriptic's fixed channel set is auto-joined once per session, per
        // docs/01_ARCHITECTURE.md §2 ("no coming-soon, everything real and
        // on by default") — no manual "join #general" step for the user.
        com.kriptic.app.mesh.ChannelRegistry.joinAllDefaults { channelId ->
            chatViewModel.joinChannel(channelId)
        }
        com.kriptic.app.mesh.KripticBridgeHolder.handler = { message ->
            markerBridge.handleIncomingMessage(message) ||
                sosTrigger.handleIncomingMessage(message) { payload -> activeSos = payload }
        }
    }

    var selectedTab by remember { mutableStateOf(KripticTab.MESSAGING) }
    var knowledgeNav by remember { mutableStateOf<KnowledgeNavState>(KnowledgeNavState.List) }
    val markers by markerRepository.observeActiveMarkers().collectAsState(initial = emptyList())

    activeSos?.let { sos ->
        AlertDialog(
            onDismissRequest = { activeSos = null },
            title = { Text("SOS from ${sos.senderNickname}") },
            text = {
                Text(
                    if (sos.lat != null && sos.lon != null) {
                        "Location: ${sos.lat}, ${sos.lon}"
                    } else {
                        "No location attached."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { activeSos = null }) { Text("Dismiss") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    // Long-press SOS trigger: per docs/01_ARCHITECTURE.md §5,
                    // "the trigger is the confirmation" — no dialog in front
                    // of this. A short tap does nothing so it can't fire by
                    // accident; only a sustained long-press sends it.
                    Text(
                        "SOS",
                        color = MaterialTheme.colorScheme.error,
                        style = com.kriptic.app.ui.theme.Heading,
                        modifier = Modifier
                            .padding(end = com.kriptic.app.ui.theme.DesignTokens.Spacing.md.dp)
                            .combinedClickableForSos {
                                sosTrigger.fire(lat = null, lon = null)
                            },
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == KripticTab.MESSAGING,
                    onClick = { selectedTab = KripticTab.MESSAGING },
                    icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                    label = { Text(KripticTab.MESSAGING.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == KripticTab.MAPS,
                    onClick = { selectedTab = KripticTab.MAPS },
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text(KripticTab.MAPS.label) },
                )
                NavigationBarItem(
                    selected = selectedTab == KripticTab.KNOWLEDGE,
                    onClick = {
                        selectedTab = KripticTab.KNOWLEDGE
                        knowledgeNav = KnowledgeNavState.List
                    },
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = null) },
                    label = { Text(KripticTab.KNOWLEDGE.label) },
                )
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                KripticTab.MESSAGING -> {
                    // The existing, inherited chat UI — untouched by this pass
                    // beyond the panic-wipe hook and theme swap described
                    // elsewhere. Kriptic's 4 default channels are auto-joined
                    // by ChannelRegistry.joinAllDefaults() at startup (wire
                    // that call in alongside mesh.startServices()).
                    ChatScreen(viewModel = chatViewModel)
                }
                KripticTab.MAPS -> {
                    MapScreen(
                        markers = markers,
                        onDropPin = { lat, lon, type, description ->
                            val marker = com.kriptic.app.map.Marker.newMarker(
                                lat = lat,
                                lon = lon,
                                type = type,
                                description = description,
                                reporterPubKey = chatViewModel.myPeerID,
                            )
                            markerBridge.broadcastMarker(marker)
                        },
                    )
                }
                KripticTab.KNOWLEDGE -> {
                    when (val nav = knowledgeNav) {
                        is KnowledgeNavState.List -> KnowledgeSearchScreen(
                            repository = knowledgeRepository,
                            onOpenEntry = { entryId -> knowledgeNav = KnowledgeNavState.Detail(entryId) },
                        )
                        is KnowledgeNavState.Detail -> KnowledgeDetailScreen(
                            repository = knowledgeRepository,
                            entryId = nav.entryId,
                            onBack = { knowledgeNav = KnowledgeNavState.List },
                        )
                    }
                }
            }
        }
    }
}
