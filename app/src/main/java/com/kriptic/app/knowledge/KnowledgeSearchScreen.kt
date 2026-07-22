package com.kriptic.app.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kriptic.app.mesh.KripticChipRow
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DesignTokens
import com.kriptic.app.ui.theme.Heading
import kotlinx.coroutines.launch

private val CATEGORY_FILTERS = listOf(
    "" to "All",
    "General" to "General",
    "Priority" to "Priority",
    "Danger-Alert" to "Danger/Alert",
    "Information" to "Information",
)

/**
 * Matches the mockup: hinted search field with a leading icon, a filter
 * chip row (same shared component as the Messaging tab's channel chips,
 * per docs/04_DESIGN_SYSTEM.md), and a scrolling list of result cards.
 */
@Composable
fun KnowledgeSearchScreen(
    repository: KnowledgeRepository,
    onOpenEntry: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { repository.ensureLoaded() } }

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    val results by produceState(initialValue = emptyList<KnowledgeEntry>(), query, selectedCategory) {
        repository.search(query, selectedCategory.ifBlank { null }).collect { value = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Knowledge",
            style = com.kriptic.app.ui.theme.Title,
            modifier = Modifier.padding(DesignTokens.Spacing.md.dp),
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Hinted search text", style = Body) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.md.dp),
        )

        Spacer(Modifier.height(DesignTokens.Spacing.sm.dp))

        KripticChipRow(
            items = CATEGORY_FILTERS,
            selectedId = selectedCategory,
            onSelect = { selectedCategory = it },
        )

        Spacer(Modifier.height(DesignTokens.Spacing.sm.dp))

        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No results.", style = Caption)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.md.dp, vertical = DesignTokens.Spacing.sm.dp),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm.dp),
            ) {
                items(results, key = { it.rowid }) { entry ->
                    Card(
                        onClick = { onOpenEntry(entry.entryId) },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(DesignTokens.Spacing.md.dp)) {
                            Text(entry.title, style = Heading, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                entry.body,
                                style = Caption,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
