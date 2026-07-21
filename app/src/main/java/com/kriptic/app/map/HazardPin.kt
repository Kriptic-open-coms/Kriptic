package com.kriptic.app.map

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class HazardType {
    WARNING, // Police line, barricade, checkpoint
    DANGER,  // Active conflict, tear gas, riot gear
    SAFE     // Safe zone, legal aid center, first-aid post
}

@Serializable
data class HazardPin(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val type: HazardType,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val reporterPubKey: String = "",
    val expiresAt: Long = System.currentTimeMillis() + (90 * 60 * 1000) // 90 mins default expiry
)
