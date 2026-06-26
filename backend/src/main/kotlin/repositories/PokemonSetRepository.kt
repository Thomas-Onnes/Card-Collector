package repositories

import models.PokemonCard
import models.PokemonSet
import java.sql.Connection

class PokemonSetRepository(
    private val databaseConnection: Connection
) {
    private val findAllQuery = "SELECT * FROM pokemon_sets"
    private val saveQuery = """
    INSERT INTO pokemon_sets (
    tcgdex_id,
    name,
    series,
    release_date
    )
    VALUES (?, ?, ?, ?)
    RETURNING id
    """.trimIndent()

    fun findAll(): List<PokemonSet>{
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val pokemonSets = arrayListOf<PokemonSet>()
        while(result.next()) {
            val pokemonSet = PokemonSet(
                result.getInt("id"),
                result.getString("tcgdex_id"),
                result.getString("name"),
                result.getString("series"),
                result.getDate("release_date")
            )
            pokemonSets.add(pokemonSet)
        }
        return pokemonSets
    }


    fun save(pokemonSet: PokemonSet): Int {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setString(1, pokemonSet.tcgDexId)
        statement.setString(2, pokemonSet.name)
        statement.setString(3, pokemonSet.series)
        statement.setDate(4, pokemonSet.releaseDate)

        val result = statement.executeQuery()

        result.next()

        val generatedCardId = result.getInt("id")

        statement.close()

        return generatedCardId
    }
}