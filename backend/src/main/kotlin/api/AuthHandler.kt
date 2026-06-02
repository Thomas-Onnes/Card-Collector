package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import repositories.UserRepository
import services.AuthService
import models.*

class AuthHandler : HttpHandler {

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
                    RegisterRequest::class.java
                )

            authService.register(request)

            val response =
                """{"message":"User created"}"""

            exchange.sendResponseHeaders(
                200,
                response.toByteArray().size.toLong()
            )

            exchange.responseBody.use {
                it.write(response.toByteArray())
            }

        } catch (e: Exception) {

            val response =
                """{"error":"${e.message}"}"""

            exchange.sendResponseHeaders(
                400,
                response.toByteArray().size.toLong()
            )

            exchange.responseBody.use {
                it.write(response.toByteArray())
            }
        }
    }
}