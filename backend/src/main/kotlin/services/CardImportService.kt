package services

import external.scryfall.ScryfallClient
import external.scryfall.mapper.toMagicTheGatheringCard
import external.scryfall.mapper.toMagicTheGatheringSet
import external.tcgdex.TcgDexClient
import external.tcgdex.mapper.TcgDexCardMapper
import external.tcgdex.mapper.TcgDexSetMapper
import models.Card
import repositories.CardRepository
import repositories.MagicTheGatheringCardRepository
import repositories.MagicTheGatheringSetRepository
import repositories.PokemonCardRepository
import repositories.PokemonSetRepository
import java.sql.Connection

class CardImportService(
    private val connection: Connection,
    private val scryfallClient: ScryfallClient = ScryfallClient(),
    private val tcgDexClient: TcgDexClient = TcgDexClient()
) {
    private val cardRepository = CardRepository()
    private val magicSetRepository = MagicTheGatheringSetRepository(connection)
    private val magicCardRepository = MagicTheGatheringCardRepository(connection)
    private val pokemonSetRepository = PokemonSetRepository(connection)
    private val pokemonCardRepository = PokemonCardRepository(connection)
    private val pokemonSetMapper = TcgDexSetMapper()
    private val pokemonCardMapper = TcgDexCardMapper()

    fun importMagicSet(setCode: String): Int {
        val cleanSetCode = setCode.trim().lowercase()
        require(cleanSetCode.matches(Regex("^[a-z0-9]{2,10}$"))) {
            "Invalid set code"
        }

        val cards = scryfallClient.getCardsBySet(cleanSetCode)
        var imported = 0

        for (cardDto in cards) {
            val magicCard = cardDto.toMagicTheGatheringCard()

            val baseCard = Card(
                id = null,
                gameType = "mtg",
                externalApiId = magicCard.scryfallId,
                name = magicCard.name,
                imageUrl = null
            )

            val generatedCardId = cardRepository.save(connection, baseCard)
            magicCardRepository.save(magicCard, generatedCardId)
            imported++
        }

        if (cards.isNotEmpty()) {
            val first = cards.first()
            magicSetRepository.save(
                first.toMagicTheGatheringSetFallback()
            )
        }

        return imported
    }

    fun importPokemonSet(setId: String): Int {
        val cleanSetId = setId.trim().lowercase()
        require(cleanSetId.matches(Regex("^[a-z0-9-]{2,30}$"))) {
            "Invalid set code"
        }

        val responseSet = tcgDexClient.getSet(cleanSetId)
        val pokemonSet = pokemonSetMapper.toPokemonSet(responseSet)
        val generatedSetId = pokemonSetRepository.save(pokemonSet)
        var imported = 0

        for (cardSummary in responseSet.dto.cards) {
            val responseCard = tcgDexClient.getCard(cardSummary.id)
            val baseCard = pokemonCardMapper.toCard(responseCard.dto)
            val generatedCardId = cardRepository.save(connection, baseCard)
            val pokemonCard = pokemonCardMapper.toPokemonCard(
                dto = responseCard.dto,
                rawJson = responseCard.rawJson,
                generatedCardId = generatedCardId,
                generatedSetId = generatedSetId
            )

            pokemonCardRepository.save(pokemonCard)
            imported++
            Thread.sleep(100)
        }

        return imported
    }

    private fun external.scryfall.dto.ScryfallCardDto.toMagicTheGatheringSetFallback(): models.MagicTheGatheringSet {
        return models.MagicTheGatheringSet(
            scryfallId = "set-${setCode.lowercase()}",
            code = setCode.lowercase(),
            name = setName
        )
    }
}
