package services

import external.tcgdex.TcgDexClient
import external.tcgdex.mapper.TcgDexCardMapper
import models.PokemonCard
import repositories.CardRepository
import repositories.PokemonCardRepository

class PokemonCardService(
    private val tcgDexClient: TcgDexClient,
    private val mapper: TcgDexCardMapper,
    private val cardRepository: CardRepository,
    private val pokemonCardRepository: PokemonCardRepository
) {

    fun importCard(cardId: String) {
        println("Fetching card from TCGDex")
        val response = tcgDexClient.getCard(cardId)
        println("Mapping DTO to model")
        val card = mapper.toCard(
            response.dto
        )
        println("Generated card: $card")
        val pokemonCard = mapper.toPokemonCard(
            response.dto,
            response.rawJson
        )
        println("Generated pokemonCard: $pokemonCard")
        println("Saving generic card")
        val generatedCardId = cardRepository.save(card)
        println("Generated card id: $generatedCardId")
        println("Saving pokemon card")
        pokemonCardRepository.save(
            pokemonCard,
            generatedCardId
        )
        println("import succesful")

    }

    fun getAllPokemonCards(): List<PokemonCard> {
        return pokemonCardRepository.findAll()
    }
}