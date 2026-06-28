package security

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InputValidatorTest {

    @Test
    fun `valid username is accepted`() {
        InputValidator.validateUsername("Thomas_123")
    }

    @Test
    fun `blank username is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateUsername("   ")
        }
    }

    @Test
    fun `too short username is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateUsername("ab")
        }
    }

    @Test
    fun `too long username is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateUsername("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        }
    }

    @Test
    fun `username with forbidden characters is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateUsername("Thomas!")
        }
    }

    @Test
    fun `valid email is accepted`() {
        InputValidator.validateEmail("test.user+1@example.com")
    }

    @Test
    fun `email without at sign is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateEmail("test.example.com")
        }
    }

    @Test
    fun `email without domain dot is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateEmail("test@example")
        }
    }

    @Test
    fun `email with whitespace is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateEmail("test user@example.com")
        }
    }

    @Test
    fun `email with invalid local part is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validateEmail(".test@example.com")
        }
    }

    @Test
    fun `valid password is accepted`() {
        InputValidator.validatePassword("Password123")
    }

    @Test
    fun `too short password is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validatePassword("short")
        }
    }

    @Test
    fun `too long password is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            InputValidator.validatePassword("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                    "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                    "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        }
    }

    @Test
    fun `isValidEmail returns true for valid email`() {
        assertTrue(InputValidator.isValidEmail("thomas@example.com"))
    }

    @Test
    fun `isValidEmail returns false for invalid email`() {
        assertFalse(InputValidator.isValidEmail("not-an-email"))
    }
}