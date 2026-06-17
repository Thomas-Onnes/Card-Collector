package repositories

import models.CollectionCard
import models.enums.CardCondition
import java.sql.Connection

class CollectionCardRepository(
    private val databaseConnection: Connection
){
    private val findAllQuery = "SELECT * FROM collection_cards"

    fun findAll(): List<CollectionCard> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val collectionCards = arrayListOf<CollectionCard>()
        while(result.next()) {
            val collectionCard = CollectionCard(
                result.getInt("id"),
                result.getInt("collection_id"),
                result.getInt("card_id"),
                result.getInt("quantity"),
                CardCondition.valueOf(result.getString("card_condition")),
                result.getBoolean("is_foil"),
                result.getString("language"),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at").toLocalDateTime()
            )
            collectionCards.add(collectionCard)
        }
        return collectionCards
    }
}