package database

import pokemon.LocalPokemonDataSource
import services.PokemonCardService

class PokemonSeeder(
    private val pokemonCardService:
    PokemonCardService,
    private val localPokemonDataSource:
    LocalPokemonDataSource
) {

    fun run() {

        println(
            "Seeding Pokemon cards"
        )

        val cards =
            localPokemonDataSource
                .getSetCards(
                    "base1"
                )

        cards.forEach { card ->

            pokemonCardService
                .importLocalCard(
                    card
                )
        }

        println(
            "Pokemon seed successful"
        )
    }
}