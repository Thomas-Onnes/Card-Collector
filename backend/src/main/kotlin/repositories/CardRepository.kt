package repositories

import database.Database
import models.Card
import models.CardSearchResultResponse
import java.sql.Connection

class CardRepository {

    fun searchCards(
        gameType: String,
        name: String,
        setQuery: String,
        collectorNumber: String,
        rarity: String,
        artistOrIllustrator: String
    ): List<CardSearchResultResponse> {
        return when (gameType.lowercase()) {
            "pokemon" -> searchPokemonCards(
                setQuery = setQuery,
                collectorNumber = collectorNumber,
                rarity = rarity,
                artist = artistOrIllustrator
            )

            "mtg" -> searchMagicCards(
                name = name,
                rarity = rarity,
                illustrator = artistOrIllustrator
            )

            else -> throw IllegalArgumentException("Invalid collection type")
        }
    }

    fun save(
        connection: Connection,
        card: Card
    ): Int {
        val sql = """
            INSERT INTO cards (
                game_type,
                external_api_id,
                name,
                image_url
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT (game_type, external_api_id)
            DO UPDATE SET
                name = EXCLUDED.name,
                image_url = EXCLUDED.image_url,
                updated_at = CURRENT_TIMESTAMP
            RETURNING id
        """.trimIndent()

        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, card.gameType)
            statement.setString(2, card.externalApiId)
            statement.setString(3, card.name)
            statement.setString(4, card.imageUrl)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return resultSet.getInt("id")
                }
            }
        }

        throw IllegalStateException("Card could not be saved")
    }

    fun findAll(connection: Connection): List<Card> {
        val cards = mutableListOf<Card>()
        val sql = "SELECT id, game_type, external_api_id, name, image_url FROM cards ORDER BY name"

        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { resultSet ->
                while (resultSet.next()) {
                    cards.add(
                        Card(
                            id = resultSet.getInt("id"),
                            gameType = resultSet.getString("game_type"),
                            externalApiId = resultSet.getString("external_api_id"),
                            name = resultSet.getString("name"),
                            imageUrl = resultSet.getString("image_url")
                        )
                    )
                }
            }
        }

        return cards
    }

    private fun searchPokemonCards(
        setQuery: String,
        collectorNumber: String,
        rarity: String,
        artist: String
    ): List<CardSearchResultResponse> {
        val results = mutableListOf<CardSearchResultResponse>()

        val normalizedSet = setQuery.trim().lowercase()
        val normalizedNumber = collectorNumber.trim().lowercase()
        val normalizedRarity = rarity.trim().lowercase()
        val normalizedArtist = artist.trim().lowercase()

        val sql = """
            SELECT
                c.id,
                c.game_type,
                c.name,
                c.image_url,
                ps.name AS set_name,
                ps.tcgdex_id AS set_code,
                p.collector_number,
                p.rarity,
                p.artist AS artist_or_illustrator,
                p.price_eur
            FROM cards c
            INNER JOIN pokemon_cards p ON p.card_id = c.id
            INNER JOIN pokemon_sets ps ON ps.id = p.set_id
            WHERE c.game_type = 'pokemon'
            AND (? = '' OR LOWER(ps.name) LIKE ? OR LOWER(ps.tcgdex_id) LIKE ?)
            AND (? = '' OR LOWER(p.collector_number) LIKE ?)
            AND (? = '' OR LOWER(p.rarity) LIKE ?)
            AND (? = '' OR LOWER(COALESCE(p.artist, '')) LIKE ?)
            ORDER BY ps.name, p.collector_number, c.name
            LIMIT 30
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, normalizedSet)
                statement.setString(2, "%$normalizedSet%")
                statement.setString(3, "%$normalizedSet%")

                statement.setString(4, normalizedNumber)
                statement.setString(5, "%$normalizedNumber%")

                statement.setString(6, normalizedRarity)
                statement.setString(7, "%$normalizedRarity%")

                statement.setString(8, normalizedArtist)
                statement.setString(9, "%$normalizedArtist%")

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val rawPrice = resultSet.getObject("price_eur") as? Number

                        results.add(
                            CardSearchResultResponse(
                                cardId = resultSet.getInt("id"),
                                gameType = resultSet.getString("game_type"),
                                name = resultSet.getString("name"),
                                imageUrl = resultSet.getString("image_url"),
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

        return results
    }

    private fun searchMagicCards(
        name: String,
        rarity: String,
        illustrator: String
    ): List<CardSearchResultResponse> {
        val results = mutableListOf<CardSearchResultResponse>()

        val normalizedName = name.trim().lowercase()
        val normalizedRarity = rarity.trim().lowercase()
        val normalizedIllustrator = illustrator.trim().lowercase()

        val sql = """
            SELECT
                c.id,
                c.game_type,
                c.name,
                c.image_url,
                m.set_name,
                m.set_code,
                NULL AS collector_number,
                m.rarity,
                m.illustrator AS artist_or_illustrator,
                m.price_eur
            FROM cards c
            INNER JOIN magic_the_gathering_cards m ON m.card_id = c.id
            WHERE c.game_type = 'mtg'
            AND (? = '' OR LOWER(c.name) LIKE ? OR LOWER(m.name) LIKE ?)
            AND (? = '' OR LOWER(m.rarity) LIKE ?)
            AND (? = '' OR LOWER(COALESCE(m.illustrator, '')) LIKE ?)
            ORDER BY m.name, m.set_name, m.rarity
            LIMIT 30
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, normalizedName)
                statement.setString(2, "%$normalizedName%")
                statement.setString(3, "%$normalizedName%")

                statement.setString(4, normalizedRarity)
                statement.setString(5, "%$normalizedRarity%")

                statement.setString(6, normalizedIllustrator)
                statement.setString(7, "%$normalizedIllustrator%")

                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val rawPrice = resultSet.getObject("price_eur") as? Number

                        results.add(
                            CardSearchResultResponse(
                                cardId = resultSet.getInt("id"),
                                gameType = resultSet.getString("game_type"),
                                name = resultSet.getString("name"),
                                imageUrl = resultSet.getString("image_url"),
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

        return results
    }
}
