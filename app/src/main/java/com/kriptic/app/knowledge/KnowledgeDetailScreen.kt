package com.kriptic.app.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DesignTokens
import com.kriptic.app.ui.theme.Title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeDetailScreen(
    repository: KnowledgeRepository,
    entryId: String,
    onBack: () -> Unit,
) {
    var entry by remember { mutableStateOf<KnowledgeEntry?>(null) }
    LaunchedEffect(entryId) { entry = repository.getByEntryId(entryId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(entry?.title ?: "", maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        entry?.let { e ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.md.dp),
            ) {
                Text(e.title, style = Title)
                Spacer(Modifier.height(DesignTokens.Spacing.sm.dp))
                AssistChip(onClick = {}, label = { Text(e.category) })
                Spacer(Modifier.height(DesignTokens.Spacing.md.dp))
                Text(e.body, style = Body)
                Spacer(Modifier.height(DesignTokens.Spacing.lg.dp))
                if (e.source.isNotBlank()) {
                    Text("Source: ${e.source}", style = Caption)
                }
                e.lastReviewed?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Last reviewed: $it", style = Caption)
                } ?: run {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Not yet reviewed by a subject-matter expert — see CONTRIBUTING.md.",
                        style = Caption,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
