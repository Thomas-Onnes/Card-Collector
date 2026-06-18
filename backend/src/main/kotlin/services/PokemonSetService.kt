package services

import external.tcgdex.TcgDexClient
import external.tcgdex.dto.TcgDexSetDto
import external.tcgdex.mapper.TcgDexCardMapper
import models.PokemonSet
import repositories.PokemonSetRepository

class PokemonSetService(
    private val tcgDexClient: TcgDexClient,
    private val mapper: TcgDexCardMapper,
    private val pokemonSetRepository: PokemonSetRepository
) {
//    fun importSet(setId: String) {
//        println("Fetching set from TCGDex")
//        val response = tcgDexClient.getSet
//    }
//
    fun importLocalSet(dto: TcgDexSetDto) {
        println("Mapping SET")

        val set = mapper.toSet(dto)
    }

    fun getAllPokemonSets(): List<PokemonSet> {
        return pokemonSetRepository.findAll()
    }
}