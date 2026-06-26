package repositories

import models.Card
import models.PokemonPriceUpdateCard
import models.enums.PokemonRarity
import java.math.BigDecimal
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

    private val findCardsForPriceUpdateQuery = """
    SELECT
        c.id,
        c.external_api_id,
        pc.price_eur
    FROM cards c
    JOIN pokemon_cards pc
        ON c.id = pc.card_id
    WHERE
        c.game_type = 'Pokemon'
        AND c.id > ?
    ORDER BY c.id
    LIMIT ?
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

    fun findPokemonCardsForPriceUpdate(startId: Int, limit: Int): List<PokemonPriceUpdateCard> {
        val statement = databaseConnection.prepareStatement(findCardsForPriceUpdateQuery)
        statement.setInt(1, startId)
        statement.setInt(2, limit)
        val result = statement.executeQuery()

        val cards = mutableListOf<PokemonPriceUpdateCard>()

        while (result.next()) {
            cards.add(
                PokemonPriceUpdateCard(
                    cardId = result.getInt("id"),
                    externalApiId = result.getString("external_api_id"),
                    currentPrice = result.getBigDecimal("price_eur")
                )
            )
        }
        result.close()
        statement.close()

        return cards
    }
}