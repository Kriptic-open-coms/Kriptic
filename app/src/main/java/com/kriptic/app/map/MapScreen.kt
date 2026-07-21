package com.kriptic.app.map

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.sp
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
import com.kriptic.app.ui.theme.Safe
import com.kriptic.app.ui.theme.Title
import com.kriptic.app.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    hazardBridge: HazardBroadcastBridge,
    onDropPin: (HazardPin) -> Unit
) {
    val hazardPins by hazardBridge.activeHazardPins.collectAsState()
    var showDropPinSheet by remember { mutableStateOf(false) }

    // Selected pin details for creation
    var selectedType by remember { mutableStateOf(HazardType.WARNING) }
    var pinNote by remember { mutableStateOf("") }

    // Delhi NCR Default Reference Point: Connaught Place (28.6139° N, 77.2090° E)
    val delhiLat = 28.6139
    val delhiLon = 77.2090

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Map Container Mock / MapLibre Native Layer View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Delhi NCR Offline Map", style = Title, color = DarkTextPrimary)
                    Text(text = "Sector 28.61° N, 77.20° E • Bundled PMTiles", style = Caption, color = Accent)
                }

                Button(
                    onClick = { showDropPinSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("+ Drop Pin", style = Body, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated Vector Map View Container with Grid Pattern
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📍 Delhi NCR Vector Map",
                            style = Heading,
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Connaught Place • India Gate • Ring Road • Noida • Gurgaon",
                            style = Caption,
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${hazardPins.size} Active Hazard Pin(s) Shared Over Mesh",
                                style = Caption,
                                color = Accent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Live Mesh Hazard Feed", style = Heading, color = DarkTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            // Live Pins List
            if (hazardPins.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active hazards reported nearby in Delhi NCR",
                        style = Body,
                        color = DarkTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hazardPins) { pin ->
                        val badgeColor = when (pin.type) {
                            HazardType.WARNING -> Warning
                            HazardType.DANGER -> Danger
                            HazardType.SAFE -> Safe
                        }
                        val label = when (pin.type) {
                            HazardType.WARNING -> "WARNING"
                            HazardType.DANGER -> "DANGER"
                            HazardType.SAFE -> "SAFE ZONE"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeColor)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = Caption.copy(fontWeight = FontWeight.Bold),
                                            color = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (pin.note.isNotBlank()) pin.note else "Reported Hazard Pin",
                                            style = Body,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            text = "Delhi NCR (Lat: ${String.format("%.4f", pin.latitude)}, Lon: ${String.format("%.4f", pin.longitude)})",
                                            style = Caption,
                                            color = DarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Drop Hazard Pin Bottom Sheet Modal
        if (showDropPinSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDropPinSheet = false },
                containerColor = DarkSurfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(text = "Report Delhi NCR Hazard Pin", style = Title, color = DarkTextPrimary)
                    Text(text = "Broadcasting to all mesh devices within radio range", style = Caption, color = DarkTextSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Select Hazard Type", style = Heading, color = DarkTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HazardTypeButton(
                            title = "Warning",
                            color = Warning,
                            isSelected = selectedType == HazardType.WARNING,
                            onClick = { selectedType = HazardType.WARNING },
                            modifier = Modifier.weight(1f)
                        )
                        HazardTypeButton(
                            title = "Danger",
                            color = Danger,
                            isSelected = selectedType == HazardType.DANGER,
                            onClick = { selectedType = HazardType.DANGER },
                            modifier = Modifier.weight(1f)
                        )
                        HazardTypeButton(
                            title = "Safe",
                            color = Safe,
                            isSelected = selectedType == HazardType.SAFE,
                            onClick = { selectedType = HazardType.SAFE },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pinNote,
                        onValueChange = { pinNote = it },
                        placeholder = { Text("Details (e.g. Police Line at Janpath, Safe Medical Desk)", style = Body, color = DarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val newPin = HazardPin(
                                latitude = delhiLat + (Math.random() - 0.5) * 0.02,
                                longitude = delhiLon + (Math.random() - 0.5) * 0.02,
                                type = selectedType,
                                note = pinNote
                            )
                            onDropPin(newPin)
                            showDropPinSheet = false
                            pinNote = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Broadcast Pin Over Mesh", style = Body.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun HazardTypeButton(
    title: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else DarkSurface)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = Body.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) Color.Black else DarkTextPrimary
        )
    }
}
