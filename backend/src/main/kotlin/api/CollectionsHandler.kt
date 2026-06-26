package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import models.AddCardToCollectionRequest
import models.CollectionCardsResponse
import models.CreateCollectionRequest
import models.RemoveCardFromCollectionRequest
import repositories.CollectionCardRepository
import repositories.UserCollectionRepository
import security.CollectionValidator
import security.TokenService

class CollectionsHandler : HttpHandler {

    private val gson = Gson()
    private val collectionRepository = UserCollectionRepository()
    private val collectionCardRepository = CollectionCardRepository()

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
            val parts = path.trim('/').split('/')

            when {
                path == "/collections" && method == "GET" -> {
                    handleGetCollections(exchange, session.userId)
                }

                path == "/collections" && method == "POST" -> {
                    handleCreateCollection(exchange, session.userId)
                }

                parts.size == 2 &&
                        parts[0] == "collections" &&
                        method == "DELETE" -> {
                    val collectionId = parts[1].toIntOrNull()

                    if (collectionId == null) {
                        HttpUtils.sendJson(
                            exchange,
                            400,
                            """{"error":"Invalid collection id"}"""
                        )
                        return
                    }

                    handleDeleteCollection(
                        exchange = exchange,
                        userId = session.userId,
                        collectionId = collectionId
                    )
                }

                parts.size == 3 &&
                        parts[0] == "collections" &&
                        parts[2] == "cards" &&
                        method == "GET" -> {
                    val collectionId = parts[1].toIntOrNull()

                    if (collectionId == null) {
                        HttpUtils.sendJson(
                            exchange,
                            400,
                            """{"error":"Invalid collection id"}"""
                        )
                        return
                    }

                    handleGetCollectionCards(
                        exchange = exchange,
                        userId = session.userId,
                        collectionId = collectionId
                    )
                }

                parts.size == 3 &&
                        parts[0] == "collections" &&
                        parts[2] == "cards" &&
                        method == "POST" -> {
                    val collectionId = parts[1].toIntOrNull()

                    if (collectionId == null) {
                        HttpUtils.sendJson(
                            exchange,
                            400,
                            """{"error":"Invalid collection id"}"""
                        )
                        return
                    }

                    handleAddCardToCollection(
                        exchange = exchange,
                        userId = session.userId,
                        collectionId = collectionId
                    )
                }

                parts.size == 5 &&
                        parts[0] == "collections" &&
                        parts[2] == "cards" &&
                        parts[4] == "remove" &&
                        method == "POST" -> {
                    val collectionId = parts[1].toIntOrNull()
                    val collectionCardId = parts[3].toIntOrNull()

                    if (collectionId == null || collectionCardId == null) {
                        HttpUtils.sendJson(
                            exchange,
                            400,
                            """{"error":"Invalid collection card id"}"""
                        )
                        return
                    }

                    handleRemoveCardFromCollection(
                        exchange = exchange,
                        userId = session.userId,
                        collectionId = collectionId,
                        collectionCardId = collectionCardId
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
                "Invalid collection type",
                "Card does not match collection type",
                "Invalid card id",
                "Invalid collection card id",
                "Quantity must be between 1 and 999",
                "Remove quantity must be between 1 and 999",
                "Card condition is required",
                "Language is required"
            )

            val message = if (allowedErrors.contains(e.message)) {
                e.message
            } else {
                "Invalid collection request"
            }

            val statusCode = if (e.message == "Request body too large") {
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
        val collections = collectionRepository.findByUserId(userId)

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
        val body = HttpUtils.readRequestBody(exchange)

        val request = gson.fromJson(
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

        val collectionName = CollectionValidator.validateCollectionName(
            request.collectionName
        )

        val gameType = CollectionValidator.normalizeGameType(
            request.gameType
        )

        val createdCollection = collectionRepository.createCollection(
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
        val deleted = collectionRepository.deleteCollection(
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

    private fun handleGetCollectionCards(
        exchange: HttpExchange,
        userId: Int,
        collectionId: Int
    ) {
        val collection = collectionRepository.findOwnedCollection(
            userId = userId,
            collectionId = collectionId
        )

        if (collection == null) {
            HttpUtils.sendJson(
                exchange,
                404,
                """{"error":"Collection not found"}"""
            )
            return
        }

        sendCollectionCardsResponse(
            exchange = exchange,
            userId = userId,
            collectionId = collectionId,
            statusCode = 200
        )
    }

    private fun handleAddCardToCollection(
        exchange: HttpExchange,
        userId: Int,
        collectionId: Int
    ) {
        val body = HttpUtils.readRequestBody(exchange)

        val request = gson.fromJson(
            body,
            AddCardToCollectionRequest::class.java
        )

        if (request == null) {
            HttpUtils.sendJson(
                exchange,
                400,
                """{"error":"Invalid collection request"}"""
            )
            return
        }

        collectionCardRepository.addCardToCollection(
            userId = userId,
            collectionId = collectionId,
            cardId = request.cardId,
            quantity = request.quantity,
            cardCondition = request.cardCondition,
            isFoil = request.isFoil,
            language = request.language
        )

        sendCollectionCardsResponse(
            exchange = exchange,
            userId = userId,
            collectionId = collectionId,
            statusCode = 201
        )
    }

    private fun handleRemoveCardFromCollection(
        exchange: HttpExchange,
        userId: Int,
        collectionId: Int,
        collectionCardId: Int
    ) {
        val body = HttpUtils.readRequestBody(exchange)

        val request = gson.fromJson(
            body,
            RemoveCardFromCollectionRequest::class.java
        )

        if (request == null) {
            HttpUtils.sendJson(
                exchange,
                400,
                """{"error":"Invalid collection request"}"""
            )
            return
        }

        collectionCardRepository.removeCardFromCollection(
            userId = userId,
            collectionId = collectionId,
            collectionCardId = collectionCardId,
            quantityToRemove = request.quantity
        )

        sendCollectionCardsResponse(
            exchange = exchange,
            userId = userId,
            collectionId = collectionId,
            statusCode = 200
        )
    }

    private fun sendCollectionCardsResponse(
        exchange: HttpExchange,
        userId: Int,
        collectionId: Int,
        statusCode: Int
    ) {
        val cards = collectionCardRepository.findCardsInCollection(
            userId = userId,
            collectionId = collectionId
        )

        val totalPrice = cards.sumOf { card ->
            (card.price ?: 0.0) * card.quantity
        }

        val response = CollectionCardsResponse(
            cards = cards,
            totalPrice = totalPrice
        )

        HttpUtils.sendJson(
            exchange,
            statusCode,
            gson.toJson(response)
        )
    }

    private fun getSession(exchange: HttpExchange): TokenService.UserSession? {
        val authHeader = exchange.requestHeaders.getFirst("Authorization")

        val token = authHeader
            ?.removePrefix("Bearer")
            ?.trim()

        return TokenService.validateToken(token)
    }
}