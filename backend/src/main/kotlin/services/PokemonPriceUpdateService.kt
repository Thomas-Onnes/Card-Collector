package services

import external.tcgdex.TcgDexClient
import models.PokemonPriceUpdateCard
import repositories.CardRepository
import repositories.PokemonCardRepository
import java.math.BigDecimal
import java.sql.Connection

class PokemonPriceUpdateService(
    private val connection: Connection,
    private val cardRepository: CardRepository,
    private val pokemonCardRepository: PokemonCardRepository,
    private val tcgDexClient: TcgDexClient
) {

    fun updateNextBatch(
        limit: Int,
        requestDelayMillis: Long
    ): Int {
        val safeLimit = if (limit > 0) limit else 50
        val cards = cardRepository.findPokemonCardsForPriceUpdate(
            connection = connection,
            limit = safeLimit
        )

        if (cards.isEmpty()) {
            println("No Pokemon cards found for price update")
            return 0
        }

        var changedPrices = 0

        for (card in cards) {
            try {
                if (updateSingleCard(card)) {
                    changedPrices++
                }
            } catch (e: Exception) {
                println(
                    "Failed to update Pokemon price for ${card.externalApiId}: " +
                            "${e::class.java.simpleName}: ${e.message}"
                )
            }

            if (requestDelayMillis > 0) {
                Thread.sleep(requestDelayMillis)
            }
        }

        println("Pokemon price update batch finished. Checked ${cards.size} cards, changed $changedPrices prices.")
        return cards.size
    }

    private fun updateSingleCard(card: PokemonPriceUpdateCard): Boolean {
        val response = tcgDexClient.getCard(card.externalApiId)
        val newPrice = response.dto.pricing?.cardMarket?.avg
        val priceChanged = !pricesAreEqual(card.currentPrice, newPrice)

        pokemonCardRepository.updatePrice(
            cardId = card.cardId,
            newPrice = newPrice
        )

        if (priceChanged) {
            println("Updating Pokemon price ${card.externalApiId}: ${card.currentPrice} -> $newPrice")
        }

        return priceChanged
    }

    private fun pricesAreEqual(
        currentPrice: BigDecimal?,
        newPrice: BigDecimal?
    ): Boolean {
        if (currentPrice == null && newPrice == null) {
            return true
        }

        if (currentPrice == null || newPrice == null) {
            return false
        }

        return currentPrice.compareTo(newPrice) == 0
    }
}