package core

import controllers.CardController
import controllers.UserCardController
import controllers.UserController
import database.DatabaseConnection
import repositories.CardRepository
import repositories.UserCardRepository
import repositories.UserRepository
import services.CardService
import services.UserCardService
import services.UserService

class App {

    fun start() {

        val connection = DatabaseConnection.getConnection()

        val userRepository = UserRepository(connection)
        val userService = UserService(userRepository)
        val userController = UserController(userService)

        val cardRepository = CardRepository(connection)
        val cardService = CardService(cardRepository)
        val cardController = CardController(cardService)

        val userCardRepository = UserCardRepository(connection)
        val userCardService = UserCardService(userCardRepository)
        val userCardController = UserCardController(userCardService)

        val router = Router(userController, cardController, userCardController)

        val request = Request(
            method = "GET",
            path = "/api/usercards"
        )

        val response = router.handle(request)

        println(response.body)
    }
}