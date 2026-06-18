package controllers

import services.PokemonSetService

class PokemonSetController (
    private val pokemonSetService: PokemonSetService
){

    fun getSet(): String {
        return pokemonSetService.getAllPokemonSets().toString()
    }
}