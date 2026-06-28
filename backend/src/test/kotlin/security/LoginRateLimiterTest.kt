package security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginRateLimiterTest {

    @Test
    fun `createAccountKey normalizes email`() {
        val key = LoginRateLimiter.createAccountKey(
            clientIp = "127.0.0.1",
            email = "  Thomas@Example.COM  "
        )

        assertTrue(key.endsWith(":thomas@example.com"))
    }

    @Test
    fun `account is blocked after maximum failed attempts`() {
        val uniqueValue = System.nanoTime().toString()
        val accountKey = "account-$uniqueValue"
        val ipKey = "ip-$uniqueValue"

        assertFalse(LoginRateLimiter.recordFailure(accountKey, ipKey))
        assertFalse(LoginRateLimiter.recordFailure(accountKey, ipKey))
        assertFalse(LoginRateLimiter.recordFailure(accountKey, ipKey))
        assertFalse(LoginRateLimiter.recordFailure(accountKey, ipKey))

        assertTrue(LoginRateLimiter.recordFailure(accountKey, ipKey))
        assertTrue(LoginRateLimiter.isBlocked(accountKey, ipKey))
    }

    @Test
    fun `successful login clears account based failures`() {
        val uniqueValue = System.nanoTime().toString()
        val accountKey = "success-account-$uniqueValue"
        val ipKey = "success-ip-$uniqueValue"

        LoginRateLimiter.recordFailure(accountKey, ipKey)
        LoginRateLimiter.recordFailure(accountKey, ipKey)

        LoginRateLimiter.recordSuccess(accountKey)

        assertFalse(LoginRateLimiter.isBlocked(accountKey, ipKey))
    }
}