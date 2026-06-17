package controllers

import services.PokemonCardService

class PokemonCardController(
    private val pokemonCardService: PokemonCardService
) {

    fun getUserCards(): String {
        return pokemonCardService.getAllPokemonCards().toString()
    }
}