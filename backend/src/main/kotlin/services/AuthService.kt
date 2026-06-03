package services

import models.RegisterRequest
import models.User
import repositories.UserRepository
import security.PasswordHasher

class AuthService(
    private val userRepository: UserRepository
) {

    fun register(request: RegisterRequest) {

        if (
            userRepository.findByUsername(
                request.username
            ) != null
        ) {

            throw Exception(
                "Username already exists"
            )
        }

        if (
            userRepository.findByEmail(
                request.email
            ) != null
        ) {

            throw Exception(
                "Email already exists"
            )
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

    fun login(request: LoginRequest): User {

        // ?: means if result returns null throw exception
        val user =
            userRepository.findByUsername(
                request.username
            ) ?: throw Exception(
                "Username or password is incorrect"
            )

        if (
            !PasswordHasher.verify(
                request.password,
                user.passwordHashed
            )
        ) {
            throw Exception(
                "Username or password is incorrect"
            )
        }

        return user
    }
}