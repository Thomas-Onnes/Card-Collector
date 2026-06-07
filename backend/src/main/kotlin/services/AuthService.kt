package services

import models.LoginRequest
import models.LoginResponse
import models.RegisterRequest
import models.User
import repositories.UserRepository
import security.PasswordHasher

class AuthService(
    private val userRepository: UserRepository
) {

    fun register(request: RegisterRequest) {

        if (
            request.username.isBlank() ||
            request.email.isBlank() ||
            request.password.isBlank()
        ) {
            throw Exception("Invalid input")
        }

        if (!request.email.contains("@")) {
            throw Exception("Invalid email")
        }

        if (request.password.length < 8) {
            throw Exception("Password too short")
        }

        if (
            userRepository.findByUsername(
                request.username
            ) != null
        ) {
            throw Exception("Username already exists")
        }

        if (
            userRepository.findByEmail(
                request.email
            ) != null
        ) {
            throw Exception("Email already exists")
        }

        val hashedPassword =
            PasswordHasher.hash(
                request.password
            )

        val user = User(
            username = request.username,
            email = request.email,
            passwordHashed = hashedPassword
        )

        userRepository.createUser(user)
    }

    fun login(request: LoginRequest): LoginResponse {

        if (
            request.email.isBlank() ||
            request.password.isBlank()
        ) {
            throw Exception("Invalid credentials")
        }

        val user =
            userRepository.findByEmail(
                request.email
            ) ?: throw Exception("Invalid credentials")

        val passwordMatches =
            PasswordHasher.verify(
                request.password,
                user.passwordHashed
            )

        if (!passwordMatches) {
            throw Exception("Invalid credentials")
        }

        return LoginResponse(
            message = "Login successful",
            userId = user.id,
            username = user.username,
            email = user.email
        )
    }
}