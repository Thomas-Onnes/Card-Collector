package models

data class LoginResponse(
    val message: String,
    val userId: Int,
    val username: String,
    val email: String
)