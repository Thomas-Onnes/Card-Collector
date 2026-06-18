package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import models.LoginRequest
import repositories.UserRepository
import services.AuthService
import security.InvalidCredentialsException
import security.TooManyLoginAttemptsException

class LoginHandler : HttpHandler {

    private val gson = Gson()
    private val authService = AuthService(UserRepository())

    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            HttpUtils.sendEmpty(exchange, 405)
            return
        }

        try {
            val body = HttpUtils.readRequestBody(exchange)
            val request = gson.fromJson(body, LoginRequest::class.java)

            if (request == null) {
                HttpUtils.sendJson(exchange, 401, """{"error":"Invalid credentials"}""")
                return
            }

            val clientIp = exchange.remoteAddress.address.hostAddress
            val loginResponse = authService.login(request, clientIp)
            val response = gson.toJson(loginResponse)

            HttpUtils.sendJson(exchange, 200, response)

        } catch (e: TooManyLoginAttemptsException) {
            HttpUtils.sendJson(
                exchange,
                429,
                """{"error":"Too many login attempts. Please try again later."}"""
            )

        } catch (e: InvalidCredentialsException) {
            HttpUtils.sendJson(
                exchange,
                401,
                """{"error":"Invalid credentials"}"""
            )

        } catch (e: IllegalArgumentException) {
            val statusCode =
                if (e.message == "Request body too large") {
                    413
                } else {
                    400
                }

            HttpUtils.sendJson(
                exchange,
                statusCode,
                """{"error":"Invalid login request"}"""
            )

        } catch (e: Exception) {
            println("Login failed: ${e::class.java.simpleName}")

            HttpUtils.sendJson(
                exchange,
                401,
                """{"error":"Invalid credentials"}"""
            )
        }
    }
}