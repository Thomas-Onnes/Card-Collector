package security

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

object TokenService {

    private const val TOKEN_BYTES = 32
    private const val SESSION_EXPIRATION_MILLIS = 60 * 60 * 1000L

    private val secureRandom = SecureRandom()

    private val sessions =
        ConcurrentHashMap<String, UserSession>()

    data class UserSession(
        val userId: Int,
        val username: String,
        val email: String,
        val expiresAt: Long
    )

    fun createSession(
        userId: Int,
        username: String,
        email: String
    ): String {
        val tokenBytes = ByteArray(TOKEN_BYTES)
        secureRandom.nextBytes(tokenBytes)

        val token =
            Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes)

        sessions[token] = UserSession(
            userId = userId,
            username = username,
            email = email,
            expiresAt = Instant.now().toEpochMilli() + SESSION_EXPIRATION_MILLIS
        )

        return token
    }

    fun validateToken(token: String?): UserSession? {
        if (token.isNullOrBlank()) {
            return null
        }

        val session = sessions[token] ?: return null

        if (session.expiresAt < Instant.now().toEpochMilli()) {
            sessions.remove(token)
            return null
        }

        return session
    }

    fun invalidateToken(token: String?) {
        if (!token.isNullOrBlank()) {
            sessions.remove(token)
        }
    }
}