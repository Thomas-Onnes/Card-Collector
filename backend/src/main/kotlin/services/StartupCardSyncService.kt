package services

import database.Database
import external.scryfall.ScryfallClient
import repositories.MagicTheGatheringCardRepository

class StartupCardSyncService {

    fun importCardsOnFirstStartup(
        pokemonSets: List<String>,
        magicSets: List<String>,
        forceImport: Boolean = false
    ) {
        if (pokemonSets.isEmpty() && magicSets.isEmpty()) {
            println("No API card sets configured for startup import")
            return
        }

        println("Checking API card startup import")

        for (setId in pokemonSets) {
            importPokemonSetIfNeeded(
                setId = setId,
                forceImport = forceImport
            )
        }

        for (setCode in magicSets) {
            importMagicSetIfNeeded(
                setCode = setCode,
                forceImport = forceImport
            )
        }

        println("API card startup import check finished")
    }

    fun updateMagicPricesOnce() {
        try {
            Database.connect().use { connection ->
                val service = MagicTheGatheringPriceUpdateService(
                    scryfallClient = ScryfallClient(),
                    magicCardRepository = MagicTheGatheringCardRepository(connection)
                )

                service.updateAllPrices()
            }
        } catch (e: Exception) {
            println("Magic price update skipped: ${e::class.java.simpleName}: ${e.message}")
        }
    }

    private fun importPokemonSetIfNeeded(
        setId: String,
        forceImport: Boolean
    ) {
        val cleanSetId = setId.trim().lowercase()

        if (cleanSetId.isBlank()) {
            return
        }

        try {
            Database.connect().use { connection ->
                if (!forceImport && pokemonSetAlreadyImported(cleanSetId)) {
                    println("Pokemon set already imported, skipping: $cleanSetId")
                    return
                }

                println("Importing Pokemon set from TCGdex: $cleanSetId")
                val imported = CardImportService(connection).importPokemonSet(cleanSetId)
                println("Pokemon set import finished: $cleanSetId ($imported cards)")
            }
        } catch (e: Exception) {
            println("Pokemon set import failed for $cleanSetId: ${e::class.java.simpleName}: ${e.message}")
        }
    }

    private fun importMagicSetIfNeeded(
        setCode: String,
        forceImport: Boolean
    ) {
        val cleanSetCode = setCode.trim().lowercase()

        if (cleanSetCode.isBlank()) {
            return
        }

        try {
            Database.connect().use { connection ->
                if (!forceImport && magicSetAlreadyImported(cleanSetCode)) {
                    println("Magic set already imported, skipping: $cleanSetCode")
                    return
                }

                println("Importing Magic set from Scryfall: $cleanSetCode")
                val imported = CardImportService(connection).importMagicSet(cleanSetCode)
                println("Magic set import finished: $cleanSetCode ($imported cards)")
            }
        } catch (e: Exception) {
            println("Magic set import failed for $cleanSetCode: ${e::class.java.simpleName}: ${e.message}")
        }
    }

    private fun pokemonSetAlreadyImported(setId: String): Boolean {
        val sql = """
            SELECT COUNT(*) AS card_count
            FROM pokemon_cards pc
            INNER JOIN pokemon_sets ps ON ps.id = pc.set_id
            WHERE LOWER(ps.tcgdex_id) = LOWER(?)
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, setId)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt("card_count") > 0
                    }
                }
            }
        }

        return false
    }

    private fun magicSetAlreadyImported(setCode: String): Boolean {
        val sql = """
            SELECT COUNT(*) AS card_count
            FROM magic_the_gathering_cards
            WHERE LOWER(set_code) = LOWER(?)
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, setCode)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return resultSet.getInt("card_count") > 0
                    }
                }
            }
        }

        return false
    }
}