package models

data class User(
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHashed: String
)