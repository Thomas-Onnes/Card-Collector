package repositories

import models.UserCollection
import java.sql.Connection

class UserCollectionRepository(
    private val databaseConnection: Connection
) {
    private val findAllQuery = "SELECT * FROM user_collections"

    fun findAll(): List<UserCollection> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val userCollections = arrayListOf<UserCollection>()
        while(result.next()) {
            val userCollection = UserCollection(
                result.getInt("id"),
                result.getInt("user_id"),
                result.getString("collection_name"),
                result.getTimestamp("created_at").toLocalDateTime(),
                result.getTimestamp("updated_at").toLocalDateTime()
            )
            userCollections.add(userCollection)
        }
        return userCollections
    }
}