package repositories

import models.PokemonCard
import models.enums.PokemonRarity
import java.sql.Connection

class PokemonCardRepository (
    private val databaseConnection: Connection
) {
    private val findAllQuery = "SELECT * FROM pokemon_cards"
    private val saveQuery = """
    INSERT INTO pokemon_cards (
    card_id,
    hp,
    rarity,
    types,
    evolves_from,
    set_name,
    set_code,
    collector_number,
    artist,
    raw_json
    )
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    fun findAll(): List<PokemonCard> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val pokemonCards = arrayListOf<PokemonCard>()
        while(result.next()) {
            val pokemonCard = PokemonCard (
                result.getInt("card_id"),
                result.getInt("hp"),
                PokemonRarity.valueOf(result.getString("rarity")),
                result.getString("types"),
                result.getString("evolves_from"),
                result.getString("set_name"),
                result.getString("set_code"),
                result.getString("collector_number"),
                result.getString("artist"),
                result.getString("raw_json")
            )
            pokemonCards.add(pokemonCard)
        }
        return pokemonCards
    }

    fun save(pokemonCard: PokemonCard, generatedCardId: Int) {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setInt(1, generatedCardId)
        statement.setObject(2, pokemonCard.hp)
        statement.setString(3, pokemonCard.rarity.toString())
        statement.setString(4, pokemonCard.types)
        statement.setString(5, pokemonCard.evolvesFrom)
        statement.setString(6, pokemonCard.setName)
        statement.setString(7, pokemonCard.setCode)
        statement.setString(8, pokemonCard.collectorNumber)
        statement.setString(9, pokemonCard.artist)
        statement.setString(10, pokemonCard.rawJson)

        statement.executeUpdate()
        statement.close()

        return
    }
}