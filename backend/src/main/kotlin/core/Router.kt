package core

import controllers.CardController
import controllers.CollectionCardController
import controllers.UserCollectionController
import controllers.UserController

class Router(
    private val userController: UserController,
    private val cardController: CardController,
    private val collectionCardController: CollectionCardController,
    private val userCollectionController: UserCollectionController
) {

    fun handle(request: Request): Response {
        return when {
            request.method == "GET" &&
                    request.path == "/api/cards" -> {

                Response(
                    statusCode = 200,
                    body = cardController.getCards()
                )
            }

            request.method == "GET" &&
                    request.path == "/api/users" -> {

                Response(
                    statusCode = 200,
                    body = userController.getUsers()
                )
            }

            request.method == "GET" &&
                    request.path == "/api/userCollections" -> {

                Response(
                    statusCode = 200,
                    body = userCollectionController.getUserCollections()
                )
            }

            request.method == "GET" &&
                    request.path == "/api/collectionCards" -> {

                Response(
                    statusCode = 200,
                    body = collectionCardController.getCollectionCards()
                )
            }

            else -> {
                Response(
                    statusCode = 404,
                    body = """{"error":"Not Found"}"""
                )
            }
        }
    }
}