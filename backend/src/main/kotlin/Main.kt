import controllers.CardController
import repositories.CardRepository
import services.CardService

fun main() {
    val repository = CardRepository()
    val service = CardService(repository)
    val controller = CardController(service)

    println("Card Collector Backend Running")
    println(controller.getCards())

}