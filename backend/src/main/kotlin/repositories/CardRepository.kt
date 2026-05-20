package repositories

import models.Card
import models.enums.CurrencyCode
import models.enums.Rarity
import java.time.LocalDateTime

class CardRepository {

    fun findAll(): List<Card> {
        return listOf(
            Card(1, "Pokemon", "tcgdex",
                "Charizard", "151", "3",
                Rarity.RARE, "filler", 100.00, CurrencyCode.EUR,
                "json", LocalDateTime.now(), LocalDateTime.now())
        )
    }
}