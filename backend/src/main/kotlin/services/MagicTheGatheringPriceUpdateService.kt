package services

import external.scryfall.ScryfallClient
import repositories.MagicTheGatheringCardRepository
import java.math.BigDecimal
import java.util.UUID

class MagicTheGatheringPriceUpdateService(
    private val scryfallClient: ScryfallClient,
    private val magicCardRepository: MagicTheGatheringCardRepository
) {
    private val maxPrice =
        BigDecimal("99999999.99")

    fun updateAllPrices() {
        val scryfallIds =
            magicCardRepository.findAllScryfallIds()
                .filter { isValidScryfallId(it) }
                .distinct()

        if (scryfallIds.isEmpty()) {
            println("No valid Magic cards found for price update")
            return
        }

        val cardDtos =
            scryfallClient.getCardsByIds(scryfallIds)

        for (cardDto in cardDtos) {
            if (!isValidScryfallId(cardDto.id)) {
                println("Skipping price update for invalid Scryfall ID")
                continue
            }

            val priceEur =
                parseValidPrice(cardDto.prices?.eur)

            magicCardRepository.updatePrice(
                cardDto.id,
                priceEur
            )
        }

        println("Magic card prices updated")
    }

    private fun isValidScryfallId(value: String): Boolean {
        return try {
            UUID.fromString(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun parseValidPrice(value: String?): BigDecimal? {
        if (value == null) {
            return null
        }

        val price =
            value.toBigDecimalOrNull()
                ?: return null

        if (price < BigDecimal.ZERO) {
            return null
        }

        if (price > maxPrice) {
            return null
        }

        return price
    }
}