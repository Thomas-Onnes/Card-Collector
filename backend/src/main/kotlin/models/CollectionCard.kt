package models

import models.enums.CardCondition
import java.time.LocalDateTime

data class CollectionCard(
    val id: Int,
    val collectionId: Int,
    val cardId: Int,
    val quantity: Int,
    val cardCondition: CardCondition,
    val isFoil: Boolean,
    val language: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
