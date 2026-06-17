package repositories

import models.MagicTheGatheringSet
import java.sql.Connection

class MagicTheGatheringSetRepository(
    private val databaseConnection: Connection
) {
    private val saveQuery = """
        INSERT INTO magic_the_gathering_sets (
            scryfall_id,
            code,
            name
        )
        VALUES (?, ?, ?)
    """.trimIndent()

    private val findByScryfallIdQuery = """
        SELECT *
        FROM magic_the_gathering_sets
        WHERE scryfall_id = ?
    """.trimIndent()

    fun save(set: MagicTheGatheringSet) {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setString(1, set.scryfallId)
        statement.setString(2, set.code)
        statement.setString(3, set.name)

        statement.executeUpdate()
        statement.close()
    }

    fun findByScryfallId(scryfallId: String): MagicTheGatheringSet? {
        val statement = databaseConnection.prepareStatement(findByScryfallIdQuery)

        statement.setString(1, scryfallId)

        val result = statement.executeQuery()

        if (result.next()) {
            val set = MagicTheGatheringSet(
                id = result.getInt("id"),
                scryfallId = result.getString("scryfall_id"),
                code = result.getString("code"),
                name = result.getString("name")
            )

            result.close()
            statement.close()

            return set
        }

        result.close()
        statement.close()

        return null
    }
}