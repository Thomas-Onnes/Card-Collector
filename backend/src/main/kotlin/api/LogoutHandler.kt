package api

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import security.TokenService

class LogoutHandler : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            HttpUtils.sendEmpty(exchange, 405)
            return
        }

        try {
            val authHeader =
                exchange.requestHeaders.getFirst("Authorization")

            val token =
                authHeader
                    ?.removePrefix("Bearer")
                    ?.trim()

            TokenService.invalidateToken(token)

            HttpUtils.sendJson(
                exchange,
                200,
                """{"message":"Logged out"}"""
            )

        } catch (e: Exception) {
            HttpUtils.sendJson(
                exchange,
                200,
                """{"message":"Logged out"}"""
            )
        }
    }
}