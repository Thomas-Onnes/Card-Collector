package core

import controllers.CardController

class Router(
    private val cardController: CardController
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

            else -> {
                Response(
                    statusCode = 404,
                    body = """{"error":"Not Found"}"""
                )
            }
        }
    }
}