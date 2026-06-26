package repositories

import models.PokemonCard
import models.enums.PokemonRarity
import java.sql.Connection

class PokemonCardRepository(
    private val databaseConnection: Connection
) {

    fun save(pokemonCard: PokemonCard) {
        val sql = """
            INSERT INTO pokemon_cards (
                card_id,
                set_id,
                hp,
                rarity,
                types,
                evolves_from,
                collector_number,
                artist,
                price_eur,
                raw_json
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (set_id, collector_number)
            DO UPDATE SET
                hp = EXCLUDED.hp,
                rarity = EXCLUDED.rarity,
                types = EXCLUDED.types,
                evolves_from = EXCLUDED.evolves_from,
                artist = EXCLUDED.artist,
                price_eur = EXCLUDED.price_eur,
                raw_json = EXCLUDED.raw_json,
                updated_at = CURRENT_TIMESTAMP
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setInt(1, requireNotNull(pokemonCard.cardId))
            statement.setInt(2, pokemonCard.setId)
            statement.setObject(3, pokemonCard.hp)
            statement.setString(4, pokemonCard.rarity.name)
            statement.setString(5, pokemonCard.types)
            statement.setString(6, pokemonCard.evolvesFrom)
            statement.setString(7, pokemonCard.collectorNumber)
            statement.setString(8, pokemonCard.artist)
            statement.setBigDecimal(9, pokemonCard.priceEur)
            statement.setString(10, pokemonCard.rawJson)
            statement.executeUpdate()
        }
    }

    fun findAll(): List<PokemonCard> {
        val cards = mutableListOf<PokemonCard>()
        val sql = "SELECT * FROM pokemon_cards"

        databaseConnection.createStatement().use { statement ->
            statement.executeQuery(sql).use { resultSet ->
                while (resultSet.next()) {
                    cards.add(
                        PokemonCard(
                            cardId = resultSet.getInt("card_id"),
                            setId = resultSet.getInt("set_id"),
                            hp = resultSet.getObject("hp") as? Int,
                            rarity = PokemonRarity.valueOf(resultSet.getString("rarity")),
                            types = resultSet.getString("types"),
                            evolvesFrom = resultSet.getString("evolves_from"),
                            collectorNumber = resultSet.getString("collector_number"),
                            artist = resultSet.getString("artist"),
                            priceEur = resultSet.getBigDecimal("price_eur"),
                            rawJson = resultSet.getString("raw_json")
                        )
                    )
                }
            }
        }

        return cards
    }
}
