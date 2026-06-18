package security

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object LoginRateLimiter {

    private const val MAX_ATTEMPTS_PER_ACCOUNT = 5
    private const val MAX_ATTEMPTS_PER_IP = 8

    private const val WINDOW_MILLIS = 60_000L
    private const val LOCK_MILLIS = 5 * 60_000L

    private data class AttemptInfo(
        var attempts: Int = 0,
        var windowStartedAt: Long = Instant.now().toEpochMilli(),
        var lockedUntil: Long = 0
    )

    private val attemptsByAccountAndIp = ConcurrentHashMap<String, AttemptInfo>()
    private val attemptsByIp = ConcurrentHashMap<String, AttemptInfo>()

    fun createAccountKey(
        clientIp: String,
        email: String
    ): String {
        return "${clientIp.trim()}:${email.trim().lowercase()}"
    }

    fun createIpKey(clientIp: String): String {
        return clientIp.trim()
    }

    fun isBlocked(
        accountKey: String,
        ipKey: String
    ): Boolean {
        return isKeyBlocked(accountKey, attemptsByAccountAndIp) ||
                isKeyBlocked(ipKey, attemptsByIp)
    }

    fun recordFailure(
        accountKey: String,
        ipKey: String
    ): Boolean {
        val accountBlocked =
            recordFailureForKey(
                key = accountKey,
                storage = attemptsByAccountAndIp,
                maxAttempts = MAX_ATTEMPTS_PER_ACCOUNT
            )

        val ipBlocked =
            recordFailureForKey(
                key = ipKey,
                storage = attemptsByIp,
                maxAttempts = MAX_ATTEMPTS_PER_IP
            )

        return accountBlocked || ipBlocked
    }

    fun recordSuccess(accountKey: String) {
        attemptsByAccountAndIp.remove(accountKey)

        // Let op: de IP-key resetten we bewust niet.
        // Anders kan een aanvaller na veel foute pogingen één goede login doen
        // en de IP-limiter resetten.
    }

    private fun isKeyBlocked(
        key: String,
        storage: ConcurrentHashMap<String, AttemptInfo>
    ): Boolean {
        val now = Instant.now().toEpochMilli()
        val info = storage[key] ?: return false

        if (info.lockedUntil > now) {
            return true
        }

        if (info.lockedUntil != 0L && info.lockedUntil <= now) {
            storage.remove(key)
            return false
        }

        return false
    }

    private fun recordFailureForKey(
        key: String,
        storage: ConcurrentHashMap<String, AttemptInfo>,
        maxAttempts: Int
    ): Boolean {
        val now = Instant.now().toEpochMilli()
        var blockedNow = false

        storage.compute(key) { _, current ->
            val info = current ?: AttemptInfo(windowStartedAt = now)

            if (now - info.windowStartedAt > WINDOW_MILLIS) {
                info.attempts = 1
                info.windowStartedAt = now
                info.lockedUntil = 0
            } else {
                info.attempts += 1
            }

            if (info.attempts >= maxAttempts) {
                info.lockedUntil = now + LOCK_MILLIS
                blockedNow = true
            }

            info
        }

        return blockedNow
    }
}