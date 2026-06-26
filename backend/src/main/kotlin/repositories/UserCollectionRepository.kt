package repositories

import database.Database
import models.UserCollectionResponse

class UserCollectionRepository {

    fun findByUserId(userId: Int): List<UserCollectionResponse> {
        val collections = mutableListOf<UserCollectionResponse>()

        val sql = """
            SELECT id, collection_name, game_type
            FROM user_collections
            WHERE user_id = ?
            ORDER BY created_at DESC
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        collections.add(
                            UserCollectionResponse(
                                collectionId = resultSet.getInt("id"),
                                collectionName = resultSet.getString("collection_name"),
                                gameType = resultSet.getString("game_type")
                            )
                        )
                    }
                }
            }
        }

        return collections
    }

    fun findOwnedCollection(
        userId: Int,
        collectionId: Int
    ): UserCollectionResponse? {
        val sql = """
            SELECT id, collection_name, game_type
            FROM user_collections
            WHERE id = ?
            AND user_id = ?
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, collectionId)
                statement.setInt(2, userId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return UserCollectionResponse(
                            collectionId = resultSet.getInt("id"),
                            collectionName = resultSet.getString("collection_name"),
                            gameType = resultSet.getString("game_type")
                        )
                    }
                }
            }
        }

        return null
    }

    fun createCollection(
        userId: Int,
        collectionName: String,
        gameType: String
    ): UserCollectionResponse {
        val sql = """
            INSERT INTO user_collections
            (user_id, collection_name, game_type)
            VALUES (?, ?, ?)
            RETURNING id, collection_name, game_type
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, userId)
                statement.setString(2, collectionName)
                statement.setString(3, gameType)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return UserCollectionResponse(
                            collectionId = resultSet.getInt("id"),
                            collectionName = resultSet.getString("collection_name"),
                            gameType = resultSet.getString("game_type")
                        )
                    }
                }
            }
        }

        throw IllegalStateException("Collection could not be created")
    }

    fun deleteCollection(
        userId: Int,
        collectionId: Int
    ): Boolean {
        val sql = """
            DELETE FROM user_collections
            WHERE id = ?
            AND user_id = ?
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, collectionId)
                statement.setInt(2, userId)

                return statement.executeUpdate() > 0
            }
        }
    }
}