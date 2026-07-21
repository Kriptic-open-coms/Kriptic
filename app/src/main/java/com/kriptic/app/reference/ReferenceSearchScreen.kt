package com.kriptic.app.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kriptic.app.ui.theme.Accent
import com.kriptic.app.ui.theme.Body
import com.kriptic.app.ui.theme.Caption
import com.kriptic.app.ui.theme.DarkBackground
import com.kriptic.app.ui.theme.DarkBorder
import com.kriptic.app.ui.theme.DarkSurface
import com.kriptic.app.ui.theme.DarkSurfaceElevated
import com.kriptic.app.ui.theme.DarkTextPrimary
import com.kriptic.app.ui.theme.DarkTextSecondary
import com.kriptic.app.ui.theme.Heading
import com.kriptic.app.ui.theme.Title

@Composable
fun ReferenceSearchScreen(
    repository: ReferenceRepository,
    onSelectArticle: (ReferenceArticle) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val searchResults = remember(searchQuery, selectedCategoryFilter) {
        repository.search(searchQuery, selectedCategoryFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(text = "Offline Knowledge Reference", style = Title, color = DarkTextPrimary)
        Text(text = "Search legal rights & emergency medical aid with zero connectivity", style = Caption, color = DarkTextSecondary)

        Spacer(modifier = Modifier.height(16.dp))

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 'tear gas', 'arrest rights', 'phone'...", style = Body, color = DarkTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                title = "All",
                isSelected = selectedCategoryFilter == null,
                onClick = { selectedCategoryFilter = null }
            )
            FilterChipItem(
                title = "Legal Rights",
                isSelected = selectedCategoryFilter == "LEGAL",
                onClick = { selectedCategoryFilter = "LEGAL" }
            )
            FilterChipItem(
                title = "First Aid",
                isSelected = selectedCategoryFilter == "FIRST_AID",
                onClick = { selectedCategoryFilter = "FIRST_AID" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results List
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No matching reference articles found", style = Body, color = DarkTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(searchResults) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectArticle(article) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (article.category == "LEGAL") Accent else Color(0xFF3DAA6B))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = article.category,
                                        style = Caption.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = article.title, style = Heading, color = DarkTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = article.summary, style = Body, color = DarkTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Accent else DarkSurface)
            .border(1.dp, if (isSelected) Accent else DarkBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = Caption.copy(fontWeight = FontWeight.Medium),
            color = if (isSelected) Color.White else DarkTextSecondary
        )
    }
}
