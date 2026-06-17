package models

import java.time.LocalDateTime

data class Card(
    val id: Int?,
    val gameType: String,
    val externalApiId: String,
    val name: String,
    val imageUrl: String?
)