package security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenServiceTest {

    @Test
    fun `createSession returns a non blank token`() {
        val token = TokenService.createSession(
            userId = 1,
            username = "Thomas",
            email = "thomas@example.com"
        )

        assertTrue(token.isNotBlank())
    }

    @Test
    fun `created token can be validated`() {
        val token = TokenService.createSession(
            userId = 2,
            username = "Tester",
            email = "tester@example.com"
        )

        val session = TokenService.validateToken(token)

        assertNotNull(session)
        assertEquals(2, session.userId)
        assertEquals("Tester", session.username)
        assertEquals("tester@example.com", session.email)
    }

    @Test
    fun `blank token is rejected`() {
        assertNull(TokenService.validateToken(""))
        assertNull(TokenService.validateToken("   "))
    }

    @Test
    fun `null token is rejected`() {
        assertNull(TokenService.validateToken(null))
    }

    @Test
    fun `unknown token is rejected`() {
        assertNull(TokenService.validateToken("unknown-token"))
    }

    @Test
    fun `invalidated token is rejected`() {
        val token = TokenService.createSession(
            userId = 3,
            username = "LogoutUser",
            email = "logout@example.com"
        )

        TokenService.invalidateToken(token)

        assertNull(TokenService.validateToken(token))
    }

    @Test
    fun `two sessions receive different tokens`() {
        val firstToken = TokenService.createSession(
            userId = 4,
            username = "UserOne",
            email = "one@example.com"
        )

        val secondToken = TokenService.createSession(
            userId = 5,
            username = "UserTwo",
            email = "two@example.com"
        )

        assertNotEquals(firstToken, secondToken)
    }
}