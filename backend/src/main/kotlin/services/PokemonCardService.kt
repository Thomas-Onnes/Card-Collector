package services

import models.PokemonCard
import repositories.PokemonCardRepository

class PokemonCardService(
    private val repository: PokemonCardRepository
) {

    fun getAllPokemonCards(): List<PokemonCard> {
        return repository.findAll()
    }
}