package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import models.CreateCollectionRequest
import repositories.UserCollectionRepository
import security.CollectionValidator
import security.TokenService

class CollectionsHandler : HttpHandler {

    private val gson = Gson()
    private val repository = UserCollectionRepository()

    override fun handle(exchange: HttpExchange) {
        try {
            val session = getSession(exchange)

            if (session == null) {
                HttpUtils.sendJson(
                    exchange,
                    401,
                    """{"error":"Unauthorized"}"""
                )
                return
            }

            val path = exchange.requestURI.path
            val method = exchange.requestMethod

            when {
                path == "/collections" && method == "GET" -> {
                    handleGetCollections(exchange, session.userId)
                }

                path == "/collections" && method == "POST" -> {
                    handleCreateCollection(exchange, session.userId)
                }

                path.startsWith("/collections/") && method == "DELETE" -> {
                    val collectionIdText =
                        path.removePrefix("/collections/")

                    val collectionId =
                        collectionIdText.toIntOrNull()

                    if (collectionId == null) {
                        HttpUtils.sendJson(
                            exchange,
                            400,
                            """{"error":"Invalid collection id"}"""
                        )
                        return
                    }

                    handleDeleteCollection(
                        exchange,
                        session.userId,
                        collectionId
                    )
                }

                else -> {
                    HttpUtils.sendEmpty(exchange, 404)
                }
            }

        } catch (e: IllegalArgumentException) {
            val allowedErrors = setOf(
                "Request body too large",
                "Collection name is required",
                "Collection name must be at least 3 characters",
                "Collection name may not be longer than 50 characters",
                "Collection name may only contain letters, numbers, spaces, _ and -",
                "Invalid collection type"
            )

            val message =
                if (allowedErrors.contains(e.message)) {
                    e.message
                } else {
                    "Invalid collection request"
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
                gson.toJson(mapOf("error" to message))
            )

        } catch (e: Exception) {
            println("Collection error: ${e::class.java.simpleName}")

            HttpUtils.sendJson(
                exchange,
                500,
                """{"error":"Collection request failed"}"""
            )
        }
    }

    private fun handleGetCollections(
        exchange: HttpExchange,
        userId: Int
    ) {
        val collections =
            repository.findByUserId(userId)

        HttpUtils.sendJson(
            exchange,
            200,
            gson.toJson(collections)
        )
    }

    private fun handleCreateCollection(
        exchange: HttpExchange,
        userId: Int
    ) {
        val body =
            HttpUtils.readRequestBody(exchange)

        val request =
            gson.fromJson(
                body,
                CreateCollectionRequest::class.java
            )

        if (request == null) {
            HttpUtils.sendJson(
                exchange,
                400,
                """{"error":"Invalid collection request"}"""
            )
            return
        }

        val collectionName =
            CollectionValidator.validateCollectionName(
                request.collectionName
            )

        val gameType =
            CollectionValidator.normalizeGameType(
                request.gameType
            )

        val createdCollection =
            repository.createCollection(
                userId = userId,
                collectionName = collectionName,
                gameType = gameType
            )

        HttpUtils.sendJson(
            exchange,
            201,
            gson.toJson(createdCollection)
        )
    }

    private fun handleDeleteCollection(
        exchange: HttpExchange,
        userId: Int,
        collectionId: Int
    ) {
        val deleted =
            repository.deleteCollection(
                userId = userId,
                collectionId = collectionId
            )

        if (!deleted) {
            HttpUtils.sendJson(
                exchange,
                404,
                """{"error":"Collection not found"}"""
            )
            return
        }

        HttpUtils.sendJson(
            exchange,
            200,
            """{"message":"Collection deleted"}"""
        )
    }

    private fun getSession(exchange: HttpExchange): TokenService.UserSession? {
        val authHeader =
            exchange.requestHeaders.getFirst("Authorization")

        val token =
            authHeader
                ?.removePrefix("Bearer")
                ?.trim()

        return TokenService.validateToken(token)
    }
}