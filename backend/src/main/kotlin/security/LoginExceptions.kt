package security

class InvalidCredentialsException : RuntimeException("Invalid credentials")

class TooManyLoginAttemptsException : RuntimeException("Too many login attempts")