package repositories

import models.Card

class CardRepository {

    fun findAll(): List<Card> {
        return listOf(
            Card(1, "Charizard", "Pokemon")
        )
    }
}