package models

import java.time.LocalDateTime

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: LocalDateTime
)
