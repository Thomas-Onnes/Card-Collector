package repositories

import models.Card
import models.enums.CurrencyCode
import models.enums.Rarity
import java.time.LocalDateTime

class CardRepository {

    fun findAll(): List<Card> {
        return listOf(
            Card(
                id = 1,
                gameType = "Pokemon",
                externalApiId = "tcgdex",
                name = "Charizard",
                setCode = "151",
                collectorNumber = "3",
                rarity = Rarity.RARE,
                imageUrl = "filler",
                price = 100.00,
                currency = CurrencyCode.EUR,
                rawJson = "json",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ), Card(
                id = 1,
                gameType = "Pokemon",
                externalApiId = "tcgdex",
                name = "Blastoise",
                setCode = "151",
                collectorNumber = "2",
                rarity = Rarity.RARE,
                imageUrl = "filler",
                price = 90.00,
                currency = CurrencyCode.EUR,
                rawJson = "json",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ), Card(
                id = 1,
                gameType = "Pokemon",
                externalApiId = "tcgdex",
                name = "Venusaur",
                setCode = "151",
                collectorNumber = "5",
                rarity = Rarity.RARE,
                imageUrl = "filler",
                price = 100.00,
                currency = CurrencyCode.EUR,
                rawJson = "json",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }
}