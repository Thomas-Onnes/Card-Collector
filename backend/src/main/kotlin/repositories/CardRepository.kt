package repositories

import database.Database
import models.CardSearchResultResponse

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
                p.set_name,
                p.set_code,
                p.collector_number,
                p.rarity,
                p.artist AS artist_or_illustrator,
                p.price
            FROM cards c
            INNER JOIN pokemon_cards p ON p.card_id = c.id
            WHERE c.game_type = 'pokemon'
            AND (? = '' OR LOWER(p.set_name) LIKE ? OR LOWER(p.set_code) LIKE ?)
            AND (? = '' OR LOWER(p.collector_number) LIKE ?)
            AND (? = '' OR LOWER(p.rarity) LIKE ?)
            AND (? = '' OR LOWER(COALESCE(p.artist, '')) LIKE ?)
            ORDER BY p.set_name, p.collector_number, c.name
            LIMIT 20
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
                        val rawPrice = resultSet.getObject("price") as? Number

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
                NULL AS price
            FROM cards c
            INNER JOIN magic_the_gathering_cards m ON m.card_id = c.id
            WHERE c.game_type = 'mtg'
            AND (? = '' OR LOWER(c.name) LIKE ? OR LOWER(m.name) LIKE ?)
            AND (? = '' OR LOWER(m.rarity) LIKE ?)
            AND (? = '' OR LOWER(COALESCE(m.illustrator, '')) LIKE ?)
            ORDER BY m.name, m.set_name, m.rarity
            LIMIT 20
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
                                price = null
                            )
                        )
                    }
                }
            }
        }

        return results
    }
}