package services

import external.scryfall.ScryfallClient
import external.scryfall.mapper.toMagicTheGatheringCard
import external.scryfall.mapper.toMagicTheGatheringSet
import models.Card
import repositories.CardRepository
import repositories.MagicTheGatheringCardRepository
import repositories.MagicTheGatheringSetRepository

class MagicTheGatheringSyncService(
    private val scryfallClient: ScryfallClient,
    private val setRepository: MagicTheGatheringSetRepository,
    private val cardRepository: CardRepository,
    private val magicCardRepository: MagicTheGatheringCardRepository
) {

    fun syncNewSetsAndCards() {
        val scryfallSets = scryfallClient.getSets()

        for (setDto in scryfallSets) {
            val set = setDto.toMagicTheGatheringSet()

            val existingSet =
                setRepository.findByScryfallId(set.scryfallId)

            if (existingSet == null) {
                val cardDtos =
                    scryfallClient.getCardsBySet(set.code)

                for (cardDto in cardDtos) {
                    val magicCard =
                        cardDto.toMagicTheGatheringCard()

                    val existingCard =
                        magicCardRepository.findByScryfallId(
                            magicCard.scryfallId
                        )

                    if (existingCard == null) {
                        val baseCard = Card(
                            id = null,
                            gameType = "MAGIC_THE_GATHERING",
                            externalApiId = magicCard.scryfallId,
                            name = magicCard.name,
                            imageUrl = null
                        )

                        val generatedCardId =
                            cardRepository.save(baseCard)

                        magicCardRepository.save(
                            magicCard,
                            generatedCardId
                        )
                    }
                }

                setRepository.save(set)
            }
        }
    }
}