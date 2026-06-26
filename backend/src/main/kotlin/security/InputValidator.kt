package security

object InputValidator {

    private val usernameAllowedCharacters = Regex("^[A-Za-z0-9_]+$")
    private val localPartAllowedCharacters = Regex("^[A-Za-z0-9._%+\\-]+$")

    fun validateUsername(username: String) {
        val value = username.trim()

        if (value.isBlank()) {
            throw IllegalArgumentException("Username is required")
        }

        if (value.length < 3) {
            throw IllegalArgumentException("Username must be at least 3 characters")
        }

        if (value.length > 30) {
            throw IllegalArgumentException("Username may not be longer than 30 characters")
        }

        if (!usernameAllowedCharacters.matches(value)) {
            throw IllegalArgumentException("Username may only contain letters, numbers and _")
        }
    }

    fun validateEmail(email: String) {
        val value = email.trim()

        if (value.isBlank()) {
            throw IllegalArgumentException("Email is required")
        }

        if (value.length > 254) {
            throw IllegalArgumentException("Email is too long")
        }

        if (value.any { it.isWhitespace() }) {
            throw IllegalArgumentException("Invalid email format")
        }

        val parts = value.split("@")

        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid email format")
        }

        val localPart = parts[0]
        val domain = parts[1]

        if (localPart.isBlank() || domain.isBlank()) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (localPart.length > 64) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (!localPartAllowedCharacters.matches(localPart)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (localPart.startsWith(".") || localPart.endsWith(".") || localPart.contains("..")) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (!domain.contains(".")) {
            throw IllegalArgumentException("Invalid email format")
        }

        val domainLabels = domain.split(".")

        if (domainLabels.any { it.isBlank() }) {
            throw IllegalArgumentException("Invalid email format")
        }

        for (label in domainLabels) {
            if (label.length > 63) {
                throw IllegalArgumentException("Invalid email format")
            }

            if (label.startsWith("-") || label.endsWith("-")) {
                throw IllegalArgumentException("Invalid email format")
            }

            if (!Regex("^[A-Za-z0-9-]+$").matches(label)) {
                throw IllegalArgumentException("Invalid email format")
            }
        }

        val topLevelDomain = domainLabels.last()

        if (!Regex("^[A-Za-z]{2,63}$").matches(topLevelDomain)) {
            throw IllegalArgumentException("Invalid email format")
        }
    }

    fun validatePassword(password: String) {
        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters")
        }

        if (password.length > 128) {
            throw IllegalArgumentException("Password may not be longer than 128 characters")
        }
    }

    fun isValidEmail(email: String): Boolean {
        return try {
            validateEmail(email)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}