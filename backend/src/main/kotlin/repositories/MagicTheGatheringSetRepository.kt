package repositories

import models.MagicTheGatheringSet
import java.sql.Connection

class MagicTheGatheringSetRepository(
    private val databaseConnection: Connection
) {

    fun save(set: MagicTheGatheringSet) {
        val sql = """
            INSERT INTO magic_the_gathering_sets (
                scryfall_id,
                code,
                name
            ) VALUES (?, ?, ?)
            ON CONFLICT (code)
            DO UPDATE SET
                scryfall_id = EXCLUDED.scryfall_id,
                name = EXCLUDED.name
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, set.scryfallId)
            statement.setString(2, set.code)
            statement.setString(3, set.name)
            statement.executeUpdate()
        }
    }

    fun findByScryfallId(scryfallId: String): MagicTheGatheringSet? {
        val sql = "SELECT id, scryfall_id, code, name FROM magic_the_gathering_sets WHERE scryfall_id = ?"

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, scryfallId)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return MagicTheGatheringSet(
                        id = resultSet.getInt("id"),
                        scryfallId = resultSet.getString("scryfall_id"),
                        code = resultSet.getString("code"),
                        name = resultSet.getString("name")
                    )
                }
            }
        }

        return null
    }
}
