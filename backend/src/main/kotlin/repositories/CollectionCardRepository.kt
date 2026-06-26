package repositories

import database.Database
import models.CollectionCardResponse
import java.sql.Connection

class CollectionCardRepository {

    private data class ExistingCollectionCard(
        val collectionCardId: Int,
        val quantity: Int
    )

    fun findCardsInCollection(
        userId: Int,
        collectionId: Int
    ): List<CollectionCardResponse> {
        val cards = mutableListOf<CollectionCardResponse>()

        val sql = """
            SELECT
                cc.id AS collection_card_id,
                cc.card_id,
                cc.quantity,
                cc.card_condition,
                cc.is_foil,
                cc.language,
                c.name,
                c.game_type,
                c.image_url,
                COALESCE(p.set_name, m.set_name) AS set_name,
                COALESCE(p.set_code, m.set_code) AS set_code,
                p.collector_number,
                COALESCE(p.rarity, m.rarity) AS rarity,
                COALESCE(p.artist, m.illustrator) AS artist_or_illustrator,
                p.price
            FROM collection_cards cc
            INNER JOIN user_collections uc ON uc.id = cc.collection_id
            INNER JOIN cards c ON c.id = cc.card_id
            LEFT JOIN pokemon_cards p ON p.card_id = c.id
            LEFT JOIN magic_the_gathering_cards m ON m.card_id = c.id
            WHERE cc.collection_id = ?
            AND uc.user_id = ?
            ORDER BY cc.created_at DESC
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, collectionId)
                statement.setInt(2, userId)

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val rawPrice = resultSet.getObject("price") as? Number

                        cards.add(
                            CollectionCardResponse(
                                collectionCardId = resultSet.getInt("collection_card_id"),
                                cardId = resultSet.getInt("card_id"),
                                name = resultSet.getString("name"),
                                gameType = resultSet.getString("game_type"),
                                imageUrl = resultSet.getString("image_url"),
                                quantity = resultSet.getInt("quantity"),
                                cardCondition = resultSet.getString("card_condition"),
                                isFoil = resultSet.getBoolean("is_foil"),
                                language = resultSet.getString("language"),
                                setName = resultSet.getString("set_name"),
                                setCode = resultSet.getString("set_code"),
                                collectorNumber = resultSet.getString("collector_number"),
                                rarity = resultSet.getString("rarity"),
                                artistOrIllustrator = resultSet.getString("artist_or_illustrator"),
                                price = rawPrice?.toDouble()
                            )
                        )
                    }
                }
            }
        }

        return cards
    }

    fun addCardToCollection(
        userId: Int,
        collectionId: Int,
        cardId: Int,
        quantity: Int,
        cardCondition: String,
        isFoil: Boolean,
        language: String
    ) {
        validateAddRequest(
            cardId = cardId,
            quantity = quantity,
            cardCondition = cardCondition,
            language = language
        )

        Database.connect().use { connection ->
            connection.autoCommit = false

            try {
                validateCardCanBeAdded(
                    connection = connection,
                    userId = userId,
                    collectionId = collectionId,
                    cardId = cardId
                )

                val existingCard = findExistingCollectionCard(
                    connection = connection,
                    collectionId = collectionId,
                    cardId = cardId,
                    cardCondition = cardCondition,
                    isFoil = isFoil,
                    language = language
                )

                if (existingCard != null) {
                    val newQuantity = existingCard.quantity + quantity

                    if (newQuantity > 999) {
                        throw IllegalArgumentException("Quantity must be between 1 and 999")
                    }

                    updateCollectionCardQuantity(
                        connection = connection,
                        collectionCardId = existingCard.collectionCardId,
                        newQuantity = newQuantity
                    )
                } else {
                    insertCollectionCard(
                        connection = connection,
                        collectionId = collectionId,
                        cardId = cardId,
                        quantity = quantity,
                        cardCondition = cardCondition,
                        isFoil = isFoil,
                        language = language
                    )
                }

                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun removeCardFromCollection(
        userId: Int,
        collectionId: Int,
        collectionCardId: Int,
        quantityToRemove: Int
    ) {
        if (collectionCardId <= 0) {
            throw IllegalArgumentException("Invalid collection card id")
        }

        if (quantityToRemove !in 1..999) {
            throw IllegalArgumentException("Remove quantity must be between 1 and 999")
        }

        Database.connect().use { connection ->
            connection.autoCommit = false

            try {
                val currentQuantity = findOwnedCollectionCardQuantity(
                    connection = connection,
                    userId = userId,
                    collectionId = collectionId,
                    collectionCardId = collectionCardId
                )

                if (currentQuantity == null) {
                    throw IllegalArgumentException("Invalid collection card id")
                }

                if (quantityToRemove >= currentQuantity) {
                    deleteCollectionCard(
                        connection = connection,
                        collectionCardId = collectionCardId
                    )
                } else {
                    updateCollectionCardQuantity(
                        connection = connection,
                        collectionCardId = collectionCardId,
                        newQuantity = currentQuantity - quantityToRemove
                    )
                }

                connection.commit()
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun validateCardCanBeAdded(
        connection: Connection,
        userId: Int,
        collectionId: Int,
        cardId: Int
    ) {
        val validationSql = """
            SELECT
                uc.game_type AS collection_game_type,
                c.game_type AS card_game_type
            FROM user_collections uc
            CROSS JOIN cards c
            WHERE uc.id = ?
            AND uc.user_id = ?
            AND c.id = ?
        """.trimIndent()

        connection.prepareStatement(validationSql).use { statement ->
            statement.setInt(1, collectionId)
            statement.setInt(2, userId)
            statement.setInt(3, cardId)

            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw IllegalArgumentException("Invalid card id")
                }

                val collectionGameType = resultSet.getString("collection_game_type")
                val cardGameType = resultSet.getString("card_game_type")

                if (collectionGameType != cardGameType) {
                    throw IllegalArgumentException("Card does not match collection type")
                }
            }
        }
    }

    private fun findExistingCollectionCard(
        connection: Connection,
        collectionId: Int,
        cardId: Int,
        cardCondition: String,
        isFoil: Boolean,
        language: String
    ): ExistingCollectionCard? {
        val sql = """
            SELECT id, quantity
            FROM collection_cards
            WHERE collection_id = ?
            AND card_id = ?
            AND LOWER(card_condition) = LOWER(?)
            AND is_foil = ?
            AND LOWER(language) = LOWER(?)
            LIMIT 1
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, collectionId)
            statement.setInt(2, cardId)
            statement.setString(3, cardCondition.trim())
            statement.setBoolean(4, isFoil)
            statement.setString(5, language.trim())

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return ExistingCollectionCard(
                        collectionCardId = resultSet.getInt("id"),
                        quantity = resultSet.getInt("quantity")
                    )
                }
            }
        }

        return null
    }

    private fun insertCollectionCard(
        connection: Connection,
        collectionId: Int,
        cardId: Int,
        quantity: Int,
        cardCondition: String,
        isFoil: Boolean,
        language: String
    ) {
        val insertSql = """
            INSERT INTO collection_cards (
                collection_id,
                card_id,
                quantity,
                card_condition,
                is_foil,
                language
            ) VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
        """.trimIndent()

        connection.prepareStatement(insertSql).use { statement ->
            statement.setInt(1, collectionId)
            statement.setInt(2, cardId)
            statement.setInt(3, quantity)
            statement.setString(4, cardCondition.trim())
            statement.setBoolean(5, isFoil)
            statement.setString(6, language.trim())

            statement.executeUpdate()
        }
    }

    private fun findOwnedCollectionCardQuantity(
        connection: Connection,
        userId: Int,
        collectionId: Int,
        collectionCardId: Int
    ): Int? {
        val sql = """
            SELECT cc.quantity
            FROM collection_cards cc
            INNER JOIN user_collections uc ON uc.id = cc.collection_id
            WHERE cc.id = ?
            AND cc.collection_id = ?
            AND uc.user_id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, collectionCardId)
            statement.setInt(2, collectionId)
            statement.setInt(3, userId)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt("quantity")
                }
            }
        }

        return null
    }

    private fun deleteCollectionCard(
        connection: Connection,
        collectionCardId: Int
    ) {
        val sql = """
            DELETE FROM collection_cards
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, collectionCardId)
            statement.executeUpdate()
        }
    }

    private fun updateCollectionCardQuantity(
        connection: Connection,
        collectionCardId: Int,
        newQuantity: Int
    ) {
        val sql = """
            UPDATE collection_cards
            SET quantity = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setInt(1, newQuantity)
            statement.setInt(2, collectionCardId)

            statement.executeUpdate()
        }
    }

    private fun validateAddRequest(
        cardId: Int,
        quantity: Int,
        cardCondition: String,
        language: String
    ) {
        if (cardId <= 0) {
            throw IllegalArgumentException("Invalid card id")
        }

        if (quantity !in 1..999) {
            throw IllegalArgumentException("Quantity must be between 1 and 999")
        }

        if (cardCondition.trim().isBlank()) {
            throw IllegalArgumentException("Card condition is required")
        }

        if (language.trim().isBlank()) {
            throw IllegalArgumentException("Language is required")
        }
    }
}