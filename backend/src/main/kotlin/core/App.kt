package core

import controllers.CardController
import repositories.CardRepository
import services.CardService

class App {

    fun start() {

        val repository = CardRepository()
        val service = CardService(repository)
        val controller = CardController(service)

        val router = Router(controller)

        val request = Request(
            method = "GET",
            path = "/api/cards"
        )

        val response = router.handle(request)

        println(response.body)
    }
}