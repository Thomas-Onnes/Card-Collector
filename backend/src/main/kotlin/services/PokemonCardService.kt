package services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import external.tcgdex.TcgDexClient
import external.tcgdex.dto.TcgDexCardDto
import external.tcgdex.mapper.TcgDexCardMapper
import models.Card
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

    fun importLocalCard(dto: TcgDexCardDto) {
        println("Mapping DTO")

        val card = mapper.toCard(dto)

        val pokemonCard = mapper.toPokemonCard(
            dto,
            jacksonObjectMapper().writeValueAsString(dto)
        )

        println("Saving card")

        val generatedCardId = cardRepository.save(card)

        pokemonCardRepository.save(pokemonCard, generatedCardId)
    }

    fun getAllPokemonCards(): List<PokemonCard> {
        return pokemonCardRepository.findAll()
    }
}