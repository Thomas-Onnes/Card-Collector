package core

import controllers.CardController
import controllers.UserCardController
import controllers.UserController

class Router(
    private val userController: UserController,
    private val cardController: CardController,
    private val userCardController: UserCardController
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
                    request.path == "/api/usercards" -> {

                Response(
                    statusCode = 200,
                    body = userCardController.getUserCards()
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