package models

import models.enums.CardCondition
import java.time.LocalDateTime

data class UserCard(
    val id: Int,
    val userId: Int,
    val cardId: Int,
    val quantity: Int,
    val condition: CardCondition,
    val isFoil: Boolean,
    val language: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
