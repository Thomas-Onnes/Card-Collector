package services

import models.Card
import repositories.CardRepository

class CardService(
    private val repository: CardRepository
) {

    fun getAllCards(): List<Card> {
        return repository.findAll()
    }
}
