package repositories

import models.MagicTheGatheringCard
import models.enums.MagicTheGatheringRarity
import java.sql.Connection

class MagicTheGatheringCardRepository(
    private val databaseConnection: Connection
) {
    private val saveQuery = """
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
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    private val findByScryfallIdQuery = """
        SELECT *
        FROM magic_the_gathering_cards
        WHERE scryfall_id = ?
    """.trimIndent()

    fun save(card: MagicTheGatheringCard, generatedCardId: Int) {
        val statement = databaseConnection.prepareStatement(saveQuery)

        statement.setInt(1, generatedCardId)
        statement.setString(2, card.scryfallId)
        statement.setString(3, card.name)
        statement.setString(4, card.setCode)
        statement.setString(5, card.setName)
        statement.setString(6, card.rarity.toString())
        statement.setString(7, card.manaCost)
        statement.setString(8, card.typeLine)
        statement.setString(9, card.illustrator)

        statement.setBoolean(10, card.isCreature)
        statement.setBoolean(11, card.isInstant)
        statement.setBoolean(12, card.isSorcery)
        statement.setBoolean(13, card.isEnchantment)
        statement.setBoolean(14, card.isArtifact)
        statement.setBoolean(15, card.isLand)
        statement.setBoolean(16, card.isPlaneswalker)
        statement.setBoolean(17, card.isLegendary)
        statement.setBoolean(18, card.isSaga)
        statement.setBoolean(19, card.isRoom)
        statement.setBoolean(20, card.isBattle)
        statement.setBoolean(21, card.isKindred)

        statement.executeUpdate()
        statement.close()
    }

    fun findByScryfallId(scryfallId: String): MagicTheGatheringCard? {
        val statement = databaseConnection.prepareStatement(findByScryfallIdQuery)

        statement.setString(1, scryfallId)

        val result = statement.executeQuery()

        if (result.next()) {
            val card = MagicTheGatheringCard(
                id = result.getInt("card_id"),
                scryfallId = result.getString("scryfall_id"),
                name = result.getString("name"),
                setCode = result.getString("set_code"),
                setName = result.getString("set_name"),
                rarity = MagicTheGatheringRarity.valueOf(
                    result.getString("rarity")
                ),
                manaCost = result.getString("mana_cost"),
                typeLine = result.getString("type_line"),
                illustrator = result.getString("illustrator"),

                isCreature = result.getBoolean("is_creature"),
                isInstant = result.getBoolean("is_instant"),
                isSorcery = result.getBoolean("is_sorcery"),
                isEnchantment = result.getBoolean("is_enchantment"),
                isArtifact = result.getBoolean("is_artifact"),
                isLand = result.getBoolean("is_land"),
                isPlaneswalker = result.getBoolean("is_planeswalker"),
                isLegendary = result.getBoolean("is_legendary"),
                isSaga = result.getBoolean("is_saga"),
                isRoom = result.getBoolean("is_room"),
                isBattle = result.getBoolean("is_battle"),
                isKindred = result.getBoolean("is_kindred")
            )

            result.close()
            statement.close()

            return card
        }

        result.close()
        statement.close()

        return null
    }
}