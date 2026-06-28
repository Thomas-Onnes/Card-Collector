package services

import models.LoginRequest
import models.RegisterRequest
import models.User
import repositories.UserRepositoryGateway
import security.InvalidCredentialsException
import security.PasswordHasher
import security.TooManyLoginAttemptsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {

    private class FakeUserRepository : UserRepositoryGateway {
        private val usersByEmail = mutableMapOf<String, User>()
        private val usersByUsername = mutableMapOf<String, User>()
        private var nextId = 1

        var createdUser: User? = null
            private set

        override fun findByEmail(email: String): User? {
            return usersByEmail[email.lowercase()]
        }

        override fun findByUsername(username: String): User? {
            return usersByUsername[username]
        }

        override fun createUser(user: User) {
            val storedUser = user.copy(
                id = if (user.id == 0) nextId++ else user.id
            )

            createdUser = storedUser
            usersByEmail[storedUser.email.lowercase()] = storedUser
            usersByUsername[storedUser.username] = storedUser
        }

        fun addUser(user: User) {
            usersByEmail[user.email.lowercase()] = user
            usersByUsername[user.username] = user
        }
    }

    @Test
    fun `register creates user with normalized email and hashed password`() {
        val repository = FakeUserRepository()
        val service = AuthService(repository)

        service.register(
            RegisterRequest(
                username = "Thomas_123",
                email = "  THOMAS@Example.com  ",
                password = "Password123!"
            )
        )

        val createdUser = assertNotNull(repository.createdUser)

        assertEquals("Thomas_123", createdUser.username)
        assertEquals("thomas@example.com", createdUser.email)
        assertNotEquals("Password123!", createdUser.passwordHashed)
        assertTrue(PasswordHasher.verify("Password123!", createdUser.passwordHashed))
    }

    @Test
    fun `register rejects duplicate username`() {
        val repository = FakeUserRepository()
        repository.addUser(
            User(
                id = 1,
                username = "Thomas",
                email = "old@example.com",
                passwordHashed = "existing-hash"
            )
        )

        val service = AuthService(repository)

        val exception =
            assertFailsWith<IllegalArgumentException> {
                service.register(
                    RegisterRequest(
                        username = "Thomas",
                        email = "new@example.com",
                        password = "Password123!"
                    )
                )
            }

        assertEquals("Username already exists", exception.message)
    }

    @Test
    fun `register rejects duplicate email`() {
        val repository = FakeUserRepository()
        repository.addUser(
            User(
                id = 1,
                username = "ExistingUser",
                email = "thomas@example.com",
                passwordHashed = "existing-hash"
            )
        )

        val service = AuthService(repository)

        val exception =
            assertFailsWith<IllegalArgumentException> {
                service.register(
                    RegisterRequest(
                        username = "NewUser",
                        email = "THOMAS@example.com",
                        password = "Password123!"
                    )
                )
            }

        assertEquals("Email already exists", exception.message)
    }

    @Test
    fun `login succeeds with correct credentials`() {
        val repository = FakeUserRepository()
        repository.addUser(
            User(
                id = 42,
                username = "Thomas",
                email = "thomas@example.com",
                passwordHashed = PasswordHasher.hash("CorrectPassword123!")
            )
        )

        val service = AuthService(repository)

        val response =
            service.login(
                request = LoginRequest(
                    email = "THOMAS@example.com",
                    password = "CorrectPassword123!"
                ),
                clientIp = uniqueClientIp()
            )

        assertEquals("Login successful", response.message)
        assertEquals(42, response.userId)
        assertEquals("Thomas", response.username)
        assertEquals("thomas@example.com", response.email)
        assertTrue(response.token.isNotBlank())
    }

    @Test
    fun `login fails with wrong password`() {
        val repository = FakeUserRepository()
        repository.addUser(
            User(
                id = 42,
                username = "Thomas",
                email = "thomas@example.com",
                passwordHashed = PasswordHasher.hash("CorrectPassword123!")
            )
        )

        val service = AuthService(repository)

        assertFailsWith<InvalidCredentialsException> {
            service.login(
                request = LoginRequest(
                    email = "thomas@example.com",
                    password = "WrongPassword123!"
                ),
                clientIp = uniqueClientIp()
            )
        }
    }

    @Test
    fun `login fails with unknown email`() {
        val repository = FakeUserRepository()
        val service = AuthService(repository)

        assertFailsWith<InvalidCredentialsException> {
            service.login(
                request = LoginRequest(
                    email = "unknown@example.com",
                    password = "Password123!"
                ),
                clientIp = uniqueClientIp()
            )
        }
    }

    @Test
    fun `login is blocked after too many failed attempts`() {
        val repository = FakeUserRepository()
        val service = AuthService(repository)
        val email = "rate-${System.nanoTime()}@example.com"
        val clientIp = uniqueClientIp()

        repeat(4) {
            assertFailsWith<InvalidCredentialsException> {
                service.login(
                    request = LoginRequest(
                        email = email,
                        password = "WrongPassword123!"
                    ),
                    clientIp = clientIp
                )
            }
        }

        assertFailsWith<TooManyLoginAttemptsException> {
            service.login(
                request = LoginRequest(
                    email = email,
                    password = "WrongPassword123!"
                ),
                clientIp = clientIp
            )
        }
    }

    private fun uniqueClientIp(): String {
        return "test-ip-${System.nanoTime()}"
    }
}