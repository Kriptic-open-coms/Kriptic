package com.kriptic.app.ui.chat

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kriptic.app.mesh.CryptoService
import com.kriptic.app.mesh.MessageEnvelope
import com.kriptic.app.mesh.MeshRouter
import com.kriptic.app.mesh.MessageType
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DarkBackground
import com.kriptic.app.ui.theme.DarkSurface
import com.kriptic.app.ui.theme.DarkSurfaceElevated
import com.kriptic.app.ui.theme.DarkTextPrimary
import com.kriptic.app.ui.theme.DarkTextSecondary
import com.kriptic.app.ui.theme.Heading
import com.kriptic.app.ui.theme.Title
import kotlinx.coroutines.launch

data class ChatMessageItem(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val isSelf: Boolean
)

@Composable
fun ChatScreen(
    meshRouter: MeshRouter,
    cryptoService: CryptoService,
    connectedPeersCount: Int,
    onSendMessage: (String) -> Unit
) {
    val messages = remember { mutableStateListOf<ChatMessageItem>() }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val myPubKey = remember { cryptoService.getPublicKeyBase64().take(8) }

    // Listen to mesh router incoming messages
    androidx.compose.runtime.LaunchedEffect(meshRouter) {
        meshRouter.incomingMessages.collect { envelope ->
            if (envelope.type == MessageType.CHAT) {
                val decryptedText = cryptoService.decryptPayload(envelope.payloadEncrypted)
                val isSelf = envelope.senderPubKey.startsWith(myPubKey)
                messages.add(
                    ChatMessageItem(
                        id = envelope.id,
                        sender = if (isSelf) "You" else envelope.senderPubKey.take(8),
                        text = decryptedText,
                        timestamp = envelope.timestamp,
                        isSelf = isSelf
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Peer Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Mesh Messaging", style = Title, color = DarkTextPrimary)
                Text(
                    text = if (connectedPeersCount > 0) "$connectedPeersCount active peer(s) in mesh range" else "Searching for nearby peers...",
                    style = Caption,
                    color = if (connectedPeersCount > 0) Accent else DarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isSelf) Alignment.End else Alignment.Start
                val cardBg = if (msg.isSelf) Accent else DarkSurfaceElevated

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Text(text = msg.sender, style = Caption, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Text(
                            text = msg.text,
                            style = Body,
                            color = DarkTextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type encrypted mesh message...", style = Body, color = DarkTextSecondary) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = DarkSurfaceElevated,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val textToSend = inputText
                        inputText = ""
                        onSendMessage(textToSend)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Send", style = Body, color = Color.White)
            }
        }
    }
}
