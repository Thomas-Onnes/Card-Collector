package security

object InputValidator {

    private val usernameRegex = Regex("^[A-Za-z0-9_]{3,30}$")
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun isValidUsername(username: String): Boolean {
        return usernameRegex.matches(username)
    }

    fun isValidEmail(email: String): Boolean {
        return email.length <= 255 && emailRegex.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length in 8..128
    }
}