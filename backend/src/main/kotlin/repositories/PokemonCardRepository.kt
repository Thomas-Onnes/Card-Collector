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
    set_id,
    hp,
    rarity,
    types,
    evolves_from,
    collector_number,
    artist,
    price_eur,
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
                result.getInt("set_id"),
                result.getInt("hp"),
                PokemonRarity.valueOf(result.getString("rarity")),
                result.getString("types"),
                result.getString("evolves_from"),
                result.getString("collector_number"),
                result.getString("artist"),
                result.getBigDecimal("price_eur"),
                result.getString("raw_json")
            )
            pokemonCards.add(pokemonCard)
        }
        return pokemonCards
    }

    fun save(pokemonCard: PokemonCard, generatedCardId: Int) {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setInt(1, generatedCardId)
        statement.setInt(2, pokemonCard.setId)
        statement.setObject(3, pokemonCard.hp)
        statement.setString(4, pokemonCard.rarity.toString())
        statement.setString(5, pokemonCard.types)
        statement.setString(6, pokemonCard.evolvesFrom)
        statement.setString(7, pokemonCard.collectorNumber)
        statement.setString(8, pokemonCard.artist)
        statement.setBigDecimal(9, pokemonCard.priceEur)
        statement.setString(10, pokemonCard.rawJson)

        statement.executeUpdate()
        statement.close()

        return
    }
}