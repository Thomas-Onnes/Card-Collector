package security

import de.mkammerer.argon2.Argon2Factory

object PasswordHasher {

    private val argon2 = Argon2Factory.create(
        Argon2Factory.Argon2Types.ARGON2id
    )

    fun hash(password: String): String {

        return argon2.hash(
            3,
            65536,
            1,
            password.toCharArray()
        )
    }

    fun verify(password: String, hash: String): Boolean {

        return argon2.verify(
            hash,
            password.toCharArray()
        )
    }
}