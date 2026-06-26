package security

import de.mkammerer.argon2.Argon2Factory
import java.util.Arrays

object PasswordHasher {

    private val argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id
    )

    fun hash(password: String): String {
        val passwordChars = password.toCharArray()

        try {
            return argon2.hash(
                3,
                65_536,
                1,
                passwordChars
            )
        } finally {
            Arrays.fill(passwordChars, '\u0000')
        }
    }

    fun verify(password: String, hash: String): Boolean {
        val passwordChars = password.toCharArray()

        try {
            return argon2.verify(
                hash,
                passwordChars
            )
        } finally {
            Arrays.fill(passwordChars, '\u0000')
        }
    }
}