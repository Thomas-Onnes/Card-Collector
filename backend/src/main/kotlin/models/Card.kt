package models

import models.enums.Rarity
import java.time.LocalDateTime

data class Card(
    val id: Int,
    val gameType: String,
    val externalApiId: String,
    val name: String,
    val setCode: String,
    val collectorNumber: String,
    val rarity: Rarity,
    val imageUrl: String,
    val price: Double,
    val currency: String,
    val rawJson: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)