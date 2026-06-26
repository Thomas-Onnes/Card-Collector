package pokemon.services

import external.tcgdex.TcgDexClient
import external.tcgdex.mapper.TcgDexCardMapper
import external.tcgdex.mapper.TcgDexSetMapper
import pokemon.PokemonSetProvider
import repositories.CardRepository
import repositories.PokemonCardRepository
import repositories.PokemonSetRepository

class PokemonImportService(
    val pokemonSetProvider: PokemonSetProvider,
    val tcgDexClient: TcgDexClient,
    val pokemonSetRepository: PokemonSetRepository,
    val pokemonSetMapper: TcgDexSetMapper,
    val cardMapper: TcgDexCardMapper,
    val cardRepository: CardRepository,
    val pokemonCardRepository: PokemonCardRepository
) {

    fun import() {
        val sets = pokemonSetProvider.getSetsToImport()

        for (set in sets) {
            val responseSet = tcgDexClient.getSet(set.id)
            val pokemonSet = pokemonSetMapper.toPokemonSet(responseSet)
            val generatedSetId = pokemonSetRepository.save(pokemonSet)

            Thread.sleep(100)
            for (cardSummary in responseSet.dto.cards) {
                val responseCard = tcgDexClient.getCard(cardSummary.id)

                val card = cardMapper.toCard(responseCard.dto)

                val generatedCardId = cardRepository.save(card)

                val pokemonCard = cardMapper.toPokemonCard(
                    dto = responseCard.dto,
                    rawJson = responseCard.rawJson,
                    generatedSetId = generatedSetId,
                    generatedCardId = generatedCardId
                )

                pokemonCardRepository.save(pokemonCard)
                Thread.sleep(100)
            }
        }
    }
}