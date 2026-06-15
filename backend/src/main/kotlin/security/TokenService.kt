package security

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

object TokenService {

    private const val TOKEN_BYTES = 32
    private const val EXPIRATION_MILLIS = 60 * 60 * 1000L

    private val secureRandom = SecureRandom()
    private val sessions = ConcurrentHashMap<String, Session>()

    data class Session(
        val userId: Int,
        val username: String,
        val email: String,
        val expiresAt: Long
    )

    fun generateToken(userId: Int, username: String, email: String): String {
        val tokenBytes = ByteArray(TOKEN_BYTES)
        secureRandom.nextBytes(tokenBytes)

        val token = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes)

        sessions[token] = Session(
            userId = userId,
            username = username,
            email = email,
            expiresAt = Instant.now().toEpochMilli() + EXPIRATION_MILLIS
        )

        return token
    }

    fun validateToken(token: String?): Session? {
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