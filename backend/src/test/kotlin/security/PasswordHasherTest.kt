package security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test
    fun `hash is not equal to original password`() {
        val password = "Password123!"
        val hash = PasswordHasher.hash(password)

        assertNotEquals(password, hash)
    }

    @Test
    fun `correct password verifies successfully`() {
        val password = "Password123!"
        val hash = PasswordHasher.hash(password)

        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `wrong password does not verify`() {
        val password = "Password123!"
        val hash = PasswordHasher.hash(password)

        assertFalse(PasswordHasher.verify("WrongPassword123!", hash))
    }
}