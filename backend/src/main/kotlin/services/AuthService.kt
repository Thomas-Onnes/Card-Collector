package services

import models.LoginRequest
import models.LoginResponse
import models.RegisterRequest
import models.User
import repositories.UserRepository
import security.InputValidator
import security.LoginRateLimiter
import security.PasswordHasher
import security.TokenService

class AuthService(
    private val userRepository: UserRepository
) {

    companion object {
        private val DUMMY_PASSWORD_HASH = PasswordHasher.hash("DummyPassword123!")
    }

    fun register(request: RegisterRequest) {
        val username = request.username.trim()
        val email = request.email.trim().lowercase()
        val password = request.password

        if (!InputValidator.isValidUsername(username)) {
            throw IllegalArgumentException("Invalid username")
        }

        if (!InputValidator.isValidEmail(email)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (!InputValidator.isValidPassword(password)) {
            throw IllegalArgumentException("Password must be at least 8 characters")
        }

        val usernameExists = userRepository.findByUsername(username) != null
        val emailExists = userRepository.findByEmail(email) != null

        if (usernameExists) {
            throw IllegalArgumentException("Username already exists")
        }

        if (emailExists) {
            throw IllegalArgumentException("Email already exists")
        }

        val hashedPassword = PasswordHasher.hash(password)

        val user = User(
            username = username,
            email = email,
            passwordHashed = hashedPassword
        )

        userRepository.createUser(user)
    }

    fun login(request: LoginRequest, clientIp: String): LoginResponse {
        val email = request.email.trim().lowercase()
        val password = request.password
        val limiterKey = "${clientIp}:${email}"

        if (LoginRateLimiter.isBlocked(limiterKey)) {
            throw Exception("Invalid credentials")
        }

        if (!InputValidator.isValidEmail(email) || password.isBlank()) {
            LoginRateLimiter.recordFailure(limiterKey)
            PasswordHasher.verify("invalid", DUMMY_PASSWORD_HASH)
            throw Exception("Invalid credentials")
        }

        val user = userRepository.findByEmail(email)
        val hashToVerify = user?.passwordHashed ?: DUMMY_PASSWORD_HASH

        val passwordMatches = PasswordHasher.verify(
            password,
            hashToVerify
        )

        if (user == null || !passwordMatches) {
            LoginRateLimiter.recordFailure(limiterKey)
            throw Exception("Invalid credentials")
        }

        LoginRateLimiter.recordSuccess(limiterKey)

        val token = TokenService.generateToken(
            userId = user.id,
            username = user.username,
            email = user.email
        )

        return LoginResponse(
            message = "Login successful",
            token = token,
            userId = user.id,
            username = user.username,
            email = user.email
        )
    }
}