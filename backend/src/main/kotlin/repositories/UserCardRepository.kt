package repositories

import models.UserCard
import models.enums.CardCondition
import java.sql.Connection

class UserCardRepository(
    private val databaseConnection: Connection
){
    private val findAllQuery = "SELECT * FROM user_cards"

    fun findAll(): List<UserCard> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val userCards = arrayListOf<UserCard>()
        while(result.next()) {
            val userCard = UserCard(
                result.getInt("id"),
                result.getInt("user_id"),
                result.getInt("card_id"),
                result.getInt("quantity"),
                CardCondition.valueOf(result.getString("card_condition")),
                result.getBoolean("is_foil"),
                result.getString("language"),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at").toLocalDateTime()
            )
            userCards.add(userCard)
        }
        return userCards
    }
}