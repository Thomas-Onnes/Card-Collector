package pokemon.services

import external.tcgdex.TcgDexClient
import repositories.CardRepository
import repositories.PokemonCardRepository

class PokemonPriceUpdateService(
    val cardRepository: CardRepository,
    val pokemonCardRepository: PokemonCardRepository,
    val tcgDexClient: TcgDexClient
) {
    private var currentCardId = 0
    private val batchSize = 50


    fun updatePrices() {
        val cards = cardRepository.findPokemonCardsForPriceUpdate(currentCardId, batchSize)

        if (cards.isEmpty()) {
            currentCardId = 0
            return
        }

        for (card in cards) {
            try {
                val response = tcgDexClient.getCard(card.externalApiId)
                val newPrice = response.dto.pricing?.cardMarket?.avg

                if (newPrice != card.currentPrice) {
                    println(
                        "Updating ${card.externalApiId}: " +
                                "${card.currentPrice} -> $newPrice"
                    )

                    pokemonCardRepository.updatePrice(
                        card.cardId,
                        newPrice
                    )
                }
            } catch (e: Exception) {
                println("Failed to update ${card.externalApiId}")
            }
            Thread.sleep(200)
        }
        currentCardId = cards.last().cardId
    }
}