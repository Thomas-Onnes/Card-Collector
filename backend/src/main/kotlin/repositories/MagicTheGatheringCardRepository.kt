package repositories

import models.MagicTheGatheringCard
import models.enums.MagicTheGatheringRarity
import java.math.BigDecimal
import java.sql.Connection

class MagicTheGatheringCardRepository(
    private val databaseConnection: Connection
) {

    fun save(
        card: MagicTheGatheringCard,
        generatedCardId: Int
    ) {
        val sql = """
            INSERT INTO magic_the_gathering_cards (
                card_id,
                scryfall_id,
                name,
                set_code,
                set_name,
                rarity,
                mana_cost,
                type_line,
                illustrator,
                price_eur,
                is_creature,
                is_instant,
                is_sorcery,
                is_enchantment,
                is_artifact,
                is_land,
                is_planeswalker,
                is_legendary,
                is_saga,
                is_room,
                is_battle,
                is_kindred
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (scryfall_id)
            DO UPDATE SET
                name = EXCLUDED.name,
                set_code = EXCLUDED.set_code,
                set_name = EXCLUDED.set_name,
                rarity = EXCLUDED.rarity,
                mana_cost = EXCLUDED.mana_cost,
                type_line = EXCLUDED.type_line,
                illustrator = EXCLUDED.illustrator,
                price_eur = EXCLUDED.price_eur,
                is_creature = EXCLUDED.is_creature,
                is_instant = EXCLUDED.is_instant,
                is_sorcery = EXCLUDED.is_sorcery,
                is_enchantment = EXCLUDED.is_enchantment,
                is_artifact = EXCLUDED.is_artifact,
                is_land = EXCLUDED.is_land,
                is_planeswalker = EXCLUDED.is_planeswalker,
                is_legendary = EXCLUDED.is_legendary,
                is_saga = EXCLUDED.is_saga,
                is_room = EXCLUDED.is_room,
                is_battle = EXCLUDED.is_battle,
                is_kindred = EXCLUDED.is_kindred
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setInt(1, generatedCardId)
            statement.setString(2, card.scryfallId)
            statement.setString(3, card.name)
            statement.setString(4, card.setCode)
            statement.setString(5, card.setName)
            statement.setString(6, card.rarity.name)
            statement.setString(7, card.manaCost)
            statement.setString(8, card.typeLine)
            statement.setString(9, card.illustrator)
            statement.setBigDecimal(10, card.priceEur)
            statement.setBoolean(11, card.isCreature)
            statement.setBoolean(12, card.isInstant)
            statement.setBoolean(13, card.isSorcery)
            statement.setBoolean(14, card.isEnchantment)
            statement.setBoolean(15, card.isArtifact)
            statement.setBoolean(16, card.isLand)
            statement.setBoolean(17, card.isPlaneswalker)
            statement.setBoolean(18, card.isLegendary)
            statement.setBoolean(19, card.isSaga)
            statement.setBoolean(20, card.isRoom)
            statement.setBoolean(21, card.isBattle)
            statement.setBoolean(22, card.isKindred)
            statement.executeUpdate()
        }
    }

    fun findByScryfallId(scryfallId: String): MagicTheGatheringCard? {
        val sql = "SELECT * FROM magic_the_gathering_cards WHERE scryfall_id = ?"

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setString(1, scryfallId)

            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) {
                    return MagicTheGatheringCard(
                        id = resultSet.getInt("card_id"),
                        scryfallId = resultSet.getString("scryfall_id"),
                        name = resultSet.getString("name"),
                        setCode = resultSet.getString("set_code"),
                        setName = resultSet.getString("set_name"),
                        rarity = MagicTheGatheringRarity.valueOf(resultSet.getString("rarity")),
                        manaCost = resultSet.getString("mana_cost"),
                        typeLine = resultSet.getString("type_line"),
                        illustrator = resultSet.getString("illustrator"),
                        priceEur = resultSet.getBigDecimal("price_eur"),
                        isCreature = resultSet.getBoolean("is_creature"),
                        isInstant = resultSet.getBoolean("is_instant"),
                        isSorcery = resultSet.getBoolean("is_sorcery"),
                        isEnchantment = resultSet.getBoolean("is_enchantment"),
                        isArtifact = resultSet.getBoolean("is_artifact"),
                        isLand = resultSet.getBoolean("is_land"),
                        isPlaneswalker = resultSet.getBoolean("is_planeswalker"),
                        isLegendary = resultSet.getBoolean("is_legendary"),
                        isSaga = resultSet.getBoolean("is_saga"),
                        isRoom = resultSet.getBoolean("is_room"),
                        isBattle = resultSet.getBoolean("is_battle"),
                        isKindred = resultSet.getBoolean("is_kindred")
                    )
                }
            }
        }

        return null
    }

    fun updatePrice(
        scryfallId: String,
        priceEur: BigDecimal?
    ) {
        val sql = """
            UPDATE magic_the_gathering_cards
            SET price_eur = ?,
                price_updated_at = CURRENT_TIMESTAMP
            WHERE scryfall_id = ?
        """.trimIndent()

        databaseConnection.prepareStatement(sql).use { statement ->
            statement.setBigDecimal(1, priceEur)
            statement.setString(2, scryfallId)
            statement.executeUpdate()
        }
    }

    fun findAllScryfallIds(): List<String> {
        val ids = mutableListOf<String>()
        val sql = "SELECT scryfall_id FROM magic_the_gathering_cards"

        databaseConnection.createStatement().use { statement ->
            statement.executeQuery(sql).use { resultSet ->
                while (resultSet.next()) {
                    ids.add(resultSet.getString("scryfall_id"))
                }
            }
        }

        return ids
    }
}
