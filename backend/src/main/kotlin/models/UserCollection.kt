package models

import java.time.LocalDateTime

data class UserCollection(
    val id: Int,
    val userId: Int,
    val collectionName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
