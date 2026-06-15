package security

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object LoginRateLimiter {

    private const val MAX_ATTEMPTS = 5
    private const val WINDOW_MILLIS = 60_000L
    private const val LOCK_MILLIS = 5 * 60_000L

    private data class AttemptInfo(
        var attempts: Int = 0,
        var windowStartedAt: Long = Instant.now().toEpochMilli(),
        var lockedUntil: Long = 0
    )

    private val attemptsByKey = ConcurrentHashMap<String, AttemptInfo>()

    fun isBlocked(key: String): Boolean {
        val now = Instant.now().toEpochMilli()
        val info = attemptsByKey[key] ?: return false

        if (info.lockedUntil > now) {
            return true
        }

        if (info.lockedUntil <= now && info.lockedUntil != 0L) {
            attemptsByKey.remove(key)
        }

        return false
    }

    fun recordFailure(key: String) {
        val now = Instant.now().toEpochMilli()

        attemptsByKey.compute(key) { _, current ->
            val info = current ?: AttemptInfo(windowStartedAt = now)

            if (now - info.windowStartedAt > WINDOW_MILLIS) {
                info.attempts = 1
                info.windowStartedAt = now
                info.lockedUntil = 0
            } else {
                info.attempts += 1
            }

            if (info.attempts >= MAX_ATTEMPTS) {
                info.lockedUntil = now + LOCK_MILLIS
            }

            info
        }
    }

    fun recordSuccess(key: String) {
        attemptsByKey.remove(key)
    }
}