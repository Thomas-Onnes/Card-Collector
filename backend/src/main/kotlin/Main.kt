import config.Environment
import config.Environment.dbMigration
import core.App
import database.DatabaseConnection
import database.MigrationRunner
import external.tcgdex.TcgDexClient
import external.tcgdex.mapper.TcgDexCardMapper
import repositories.CardRepository
import repositories.PokemonCardRepository
import config.Environment.dbSeed
import config.Environment.selectedPokemonSets
import database.DatabaseSeeder
import external.scryfall.ScryfallClient
import external.tcgdex.mapper.TcgDexSetMapper
import pokemon.import.PokemonImportService
import pokemon.import.PokemonSetProvider
import repositories.MagicTheGatheringCardRepository
import repositories.MagicTheGatheringSetRepository
import repositories.PokemonSetRepository
import services.MagicTheGatheringSyncService
import services.MagicTheGatheringPriceUpdateService
import services.MagicTheGatheringPriceUpdateScheduler

fun main() {
    val connection = DatabaseConnection.getConnection()
    println("Database connected")
    println(System.getenv("SELECTED_POKEMON_SETS"))
    println(Environment.selectedPokemonSets)
    try {
        if (dbMigration) {
            println("Running migrations")
            val migrationRunner = MigrationRunner(connection)
            migrationRunner.run()
        }
        println(dbSeed)
        if (dbSeed) {
            println("Running seeders")
            val databaseSeeder = DatabaseSeeder(connection)
            databaseSeeder.run()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }
    println("Card Collector Backend Running")

    println("Syncing Magic The Gathering sets and cards")

//    val scryfallClient = ScryfallClient()
//    val magicSetRepository = MagicTheGatheringSetRepository(connection)
//    val magicCardRepository = MagicTheGatheringCardRepository(connection)
    val cardRepository = CardRepository(connection)

//    val magicSyncService = MagicTheGatheringSyncService(
//        scryfallClient,
//        magicSetRepository,
//        cardRepository,
//        magicCardRepository
//    )

//    magicSyncService.syncNewSetsAndCards()

    println("Magic The Gathering sync finished")

//    val priceUpdateService = MagicTheGatheringPriceUpdateService(
//        scryfallClient,
//        magicCardRepository
//    )
//
//    val priceUpdateScheduler = MagicTheGatheringPriceUpdateScheduler(
//        priceUpdateService
//    )
//
//    priceUpdateScheduler.start()

    println("Testing the new code")
    val pokemonSetRepository = PokemonSetRepository(connection)
    val pokemonCardRepository = PokemonCardRepository(connection)

    val tcgDexClient = TcgDexClient()

    val pokemonSetProvider = PokemonSetProvider(tcgDexClient)

    val pokemonSetMapper = TcgDexSetMapper()
    val pokemonCardMapper = TcgDexCardMapper()

    val pokemonImportService = PokemonImportService(
        pokemonSetProvider = pokemonSetProvider,
        tcgDexClient = tcgDexClient,
        pokemonSetRepository = pokemonSetRepository,
        pokemonSetMapper = pokemonSetMapper,
        cardMapper = pokemonCardMapper,
        cardRepository = cardRepository,
        pokemonCardRepository = pokemonCardRepository
    )

    val sets = pokemonSetProvider.getSetsToImport()
    pokemonImportService.import()
    println("Aantal sets: ${sets.size}")

    sets.forEach {
        println(it.id)
    }

    println("END of the new tested code")
//    pokemonImportService.import()
    App(connection).start()

//    connection.close()
//
//    println("Connection closed")

}