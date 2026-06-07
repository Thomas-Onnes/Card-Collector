package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import models.LoginRequest
import repositories.UserRepository
import services.AuthService

class LoginHandler : HttpHandler {

    private val gson = Gson()

    private val authService =
        AuthService(
            UserRepository()
        )

    override fun handle(exchange: HttpExchange) {

        if (exchange.requestMethod != "POST") {
            exchange.sendResponseHeaders(
                405,
                -1
            )

            return
        }

        try {
            val body =
                exchange.requestBody
                    .bufferedReader()
                    .readText()

            val request =
                gson.fromJson(
                    body,
                    LoginRequest::class.java
                )

            val loginResponse =
                authService.login(request)

            val response =
                gson.toJson(loginResponse)

            exchange.responseHeaders.add(
                "Content-Type",
                "application/json"
            )

            exchange.sendResponseHeaders(
                200,
                response.toByteArray().size.toLong()
            )

            exchange.responseBody.use {
                it.write(response.toByteArray())
            }

        } catch (e: Exception) {

            val response =
                """{"error":"Invalid credentials"}"""

            exchange.responseHeaders.add(
                "Content-Type",
                "application/json"
            )

            exchange.sendResponseHeaders(
                401,
                response.toByteArray().size.toLong()
            )

            exchange.responseBody.use {
                it.write(response.toByteArray())
            }
        }
    }
}