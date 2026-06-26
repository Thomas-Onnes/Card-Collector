package repositories

import models.PokemonSet
import java.sql.Connection

class PokemonSetRepository(
    private val databaseConnection: Connection
) {

    fun save(pokemonSet: PokemonSet): Int {
        val sql = """
            INSERT INTO pokemon_sets (
                tcgdex_id,
                name,
                series,
                release_date
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT (tcgdex_id)
            DO UPDATE SET
                name = EXCLUDED.name,
                series = EXCLUDED.series,
                release_date = EXCLUDED.release_date,
                updated_at = CURRENT_TIMESTAMP
            RETURNING id
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, pokemonSet.tcgDexId)
            statement.setString(2, pokemonSet.name)
            statement.setString(3, pokemonSet.series)
            statement.setDate(4, pokemonSet.releaseDate)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt("id")
                }
            }
        }

        throw IllegalStateException("Pokemon set could not be saved")
    }

    fun findAll(): List<PokemonSet> {
        val sets = mutableListOf<PokemonSet>()
        val sql = "SELECT id, tcgdex_id, name, series, release_date FROM pokemon_sets ORDER BY name"

        databaseConnection.createStatement().use { statement ->
            statement.executeQuery(sql).use { resultSet ->
                while (resultSet.next()) {
                    sets.add(
                        PokemonSet(
                            id = resultSet.getInt("id"),
                            tcgDexId = resultSet.getString("tcgdex_id"),
                            name = resultSet.getString("name"),
                            series = resultSet.getString("series"),
                            releaseDate = resultSet.getDate("release_date")
                        )
                    )
                }
            }
        }

        return sets
    }
}
