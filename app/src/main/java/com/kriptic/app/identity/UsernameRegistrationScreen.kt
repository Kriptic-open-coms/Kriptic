package com.kriptic.app.identity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DesignTokens
import com.kriptic.app.ui.theme.Title

/**
 * First-launch screen: choose a username, once, forever (until reinstall
 * or panic wipe). See docs/01_ARCHITECTURE.md §1 for why this is immutable.
 *
 * [existingPeerNicknames] should be whatever mesh peers are currently
 * visible, if any are already in range during onboarding — used only for
 * the local collision check described in KripticIdentityRepository.
 */
@Composable
fun UsernameRegistrationScreen(
    repository: KripticIdentityRepository,
    existingPeerNicknames: List<String> = emptyList(),
    onRegistered: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignTokens.Spacing.lg.dp)
            .padding(top = DesignTokens.Spacing.xxl.dp),
    ) {
        Text("Choose a username", style = Title)
        Spacer(Modifier.height(DesignTokens.Spacing.sm.dp))
        Text(
            "This is permanent — it can't be changed later without reinstalling " +
                "the app. There's no central account system, so this name is only " +
                "guaranteed unique among the mesh peers you're currently in range " +
                "of, not across every Kriptic user everywhere.",
            style = Caption,
        )

        Spacer(Modifier.height(DesignTokens.Spacing.xl.dp))

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                error = null
            },
            singleLine = true,
            label = { Text("Username", style = Body) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
        )

        error?.let {
            Spacer(Modifier.height(DesignTokens.Spacing.xs.dp))
            Text(it, style = Caption, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(DesignTokens.Spacing.lg.dp))

        Button(
            onClick = {
                val trimmed = input.trim()
                when {
                    !repository.isValidUsername(trimmed) ->
                        error = "3-20 characters, letters/numbers/underscore, must start with a letter."
                    repository.isTakenByVisiblePeer(trimmed, existingPeerNicknames) ->
                        error = "Someone nearby is already using that name."
                    else -> {
                        val ok = repository.setUsernameOnce(trimmed)
                        if (ok) onRegistered(trimmed) else error = "Could not register — try again."
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Confirm")
        }

        Spacer(Modifier.weight(1f))

        Text(
            "Your identity is a locally generated keypair, not a phone number " +
                "or account. Nothing here is sent anywhere unless you send a message.",
            style = Caption,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = DesignTokens.Spacing.lg.dp),
        )
    }
}
