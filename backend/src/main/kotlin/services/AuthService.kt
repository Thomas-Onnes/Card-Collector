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
import security.InvalidCredentialsException
import security.TooManyLoginAttemptsException

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

        InputValidator.validateUsername(username)
        InputValidator.validateEmail(email)
        InputValidator.validatePassword(password)

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

        val accountKey =
            LoginRateLimiter.createAccountKey(
                clientIp = clientIp,
                email = email
            )

        val ipKey =
            LoginRateLimiter.createIpKey(
                clientIp = clientIp
            )

        if (LoginRateLimiter.isBlocked(accountKey, ipKey)) {
            throw TooManyLoginAttemptsException()
        }

        if (!InputValidator.isValidEmail(email) || password.isBlank()) {
            val isNowBlocked =
                LoginRateLimiter.recordFailure(
                    accountKey = accountKey,
                    ipKey = ipKey
                )

            PasswordHasher.verify(
                "invalid",
                DUMMY_PASSWORD_HASH
            )

            if (isNowBlocked) {
                throw TooManyLoginAttemptsException()
            }

            throw InvalidCredentialsException()
        }

        val user = userRepository.findByEmail(email)
        val hashToVerify = user?.passwordHashed ?: DUMMY_PASSWORD_HASH

        val passwordMatches =
            PasswordHasher.verify(
                password,
                hashToVerify
            )

        if (user == null || !passwordMatches) {
            val isNowBlocked =
                LoginRateLimiter.recordFailure(
                    accountKey = accountKey,
                    ipKey = ipKey
                )

            if (isNowBlocked) {
                throw TooManyLoginAttemptsException()
            }

            throw InvalidCredentialsException()
        }

        LoginRateLimiter.recordSuccess(accountKey)

        val token =
            TokenService.createSession(
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