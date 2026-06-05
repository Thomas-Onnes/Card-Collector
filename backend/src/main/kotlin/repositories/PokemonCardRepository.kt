package repositories

import models.PokemonCard
import models.enums.Rarity
import java.sql.Connection

class PokemonCardRepository (
    private val databaseConnection: Connection
) {
    private val findAllQuery = "SELECT * FROM pokemon_cards"

    fun findAll(): List<PokemonCard> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val pokemonCards = arrayListOf<PokemonCard>()
        while(result.next()) {
            val pokemonCard = PokemonCard (
                result.getInt("card_id"),
                result.getInt("hp"),
                Rarity.valueOf(result.getString("rarity")),
                result.getString("types"),
                result.getString("evolves_from"),
                result.getString("set_name"),
                result.getString("set_code"),
                result.getString("collector_number"),
                result.getString("artist")
            )
            pokemonCards.add(pokemonCard)
        }
        return pokemonCards
    }
}