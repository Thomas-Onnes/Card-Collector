package core

import controllers.CardController
import controllers.CollectionCardController
import controllers.UserCollectionController
import controllers.UserController
import database.DatabaseConnection
import repositories.CardRepository
import repositories.CollectionCardRepository
import repositories.UserCollectionRepository
import repositories.UserRepository
import services.CardService
import services.CollectionCardService
import services.UserCollectionService
import services.UserService
import java.sql.Connection

class App(
    private val connection: Connection
) {

    fun start() {

        val userRepository = UserRepository(connection)
        val userService = UserService(userRepository)
        val userController = UserController(userService)

        val cardRepository = CardRepository(connection)
        val cardService = CardService(cardRepository)
        val cardController = CardController(cardService)

        val collectionCardRepository = CollectionCardRepository(connection)
        val collectionCardService = CollectionCardService(collectionCardRepository)
        val collectionCardController = CollectionCardController(collectionCardService)

        val userCollectionRepository = UserCollectionRepository(connection)
        val userCollectionService = UserCollectionService(userCollectionRepository)
        val userCollectionController = UserCollectionController(userCollectionService)

        val router = Router(userController, cardController, collectionCardController, userCollectionController)

        val request = Request(
            method = "GET",
            path = "/api/collectionCards"
        )

        val response = router.handle(request)

        println(response.body)
    }
}