package com.kriptic.app.identity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameRegistrationScreen(
    onIdentityCreated: (Identity) -> Unit
) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Kriptic",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Semibold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose a username. This cannot be changed later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (it.text.length <= 20 && it.text.all { c -> c.isLetterOrDigit() || c == '_' }) {
                        username = it
                        error = null
                    }
                },
                label = { Text("Username") },
                placeholder = { Text("3-20 characters, letters/numbers/underscore") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No phone number. No email. No account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val name = username.text.trim()
                    if (name.length < 3) {
                        error = "Username must be at least 3 characters"
                        return@Button
                    }
                    isLoading = true
                    onIdentityCreated(Identity(name, ByteArray(32), ByteArray(64)))
                },
                enabled = !isLoading && username.text.trim().length >= 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesignTokens.AccentColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Identity", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Note: Username uniqueness is enforced only within your local mesh. Global uniqueness is not guaranteed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
