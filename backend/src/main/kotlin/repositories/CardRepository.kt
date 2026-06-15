package repositories

import models.Card
import models.enums.PokemonRarity
import java.sql.Connection

class CardRepository(
    private val databaseConnection: Connection
) {

    private val findAllQuery = "SELECT * FROM cards"
    private val saveQuery = """
    INSERT INTO cards (
        game_type,
        external_api_id,
        name,
        image_url
    )
    VALUES (?, ?, ?, ?)
    RETURNING id
""".trimIndent()

    fun findAll(): List<Card> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val cards  = arrayListOf<Card>()
        while(result.next()) {
            val card = Card(
                result.getInt("id"),
                result.getString("game_type"),
                result.getString("external_api_id"),
                result.getString("name"),
                result.getString("image_url"),
            )
            cards.add(card)
        }
        return cards
    }

    fun save(card: Card): Int {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setString(1, card.gameType)
        statement.setString(2, card.externalApiId)
        statement.setString(3, card.name)
        statement.setString(4, card.imageUrl)

        val result = statement.executeQuery()

        result.next()

        val generatedCardId = result.getInt("id")

        statement.close()

        return generatedCardId
    }
}