package repositories

import models.Card
import models.enums.CurrencyCode
import models.enums.Rarity
import java.sql.Connection

class CardRepository(
    private val databaseConnection: Connection
) {

    private val findAllQuery = "SELECT * FROM cards"

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
                result.getString("set_code"),
                result.getString("collector_number"),
                Rarity.valueOf(result.getString("rarity")),
                result.getString("image_url"),
                result.getDouble("price"),
                CurrencyCode.valueOf(result.getString("currency")),
                result.getString("raw_json"),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at").toLocalDateTime()
            )
            cards.add(card)
        }
        return cards
    }
}