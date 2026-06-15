package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import models.RegisterRequest
import repositories.UserRepository
import services.AuthService

class AuthHandler : HttpHandler {

    private val gson = Gson()
    private val authService = AuthService(UserRepository())

    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            HttpUtils.sendEmpty(exchange, 405)
            return
        }

        try {
            val body = HttpUtils.readRequestBody(exchange)
            val request = gson.fromJson(body, RegisterRequest::class.java)

            if (request == null) {
                HttpUtils.sendJson(
                    exchange,
                    400,
                    """{"error":"Registration failed"}"""
                )
                return
            }

            authService.register(request)

            HttpUtils.sendJson(
                exchange,
                200,
                """{"message":"User created"}"""
            )

        } catch (e: IllegalArgumentException) {
            val allowedErrors = setOf(
                "Request body too large",
                "Invalid username",
                "Invalid email format",
                "Password must be at least 8 characters",
                "Username already exists",
                "Email already exists"
            )

            val message =
                if (allowedErrors.contains(e.message)) {
                    e.message
                } else {
                    "Registration failed"
                }

            val statusCode =
                if (e.message == "Request body too large") {
                    413
                } else {
                    400
                }

            HttpUtils.sendJson(
                exchange,
                statusCode,
                """{"error":"$message"}"""
            )

        } catch (e: Exception) {
            println("Registration failed: ${e::class.java.simpleName}")

            HttpUtils.sendJson(
                exchange,
                500,
                """{"error":"Registration failed"}"""
            )
        }
    }
}