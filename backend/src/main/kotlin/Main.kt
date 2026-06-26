import config.Environment.dbMigration
import core.App
import database.DatabaseConnection
import database.MigrationRunner
import external.tcgdex.TcgDexClient
import external.tcgdex.mapper.TcgDexCardMapper
import repositories.CardRepository
import repositories.PokemonCardRepository
import services.PokemonCardService
import config.Environment.dbSeed
import database.DatabaseSeeder
import database.PokemonSeeder
import external.scryfall.ScryfallClient
import pokemon.LocalPokemonDataSource
import repositories.MagicTheGatheringCardRepository
import repositories.MagicTheGatheringSetRepository
import services.MagicTheGatheringSyncService
import services.MagicTheGatheringPriceUpdateService
import services.MagicTheGatheringPriceUpdateScheduler

fun main() {
    val connection = DatabaseConnection.getConnection()
    println("Database connected")

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

    val scryfallClient = ScryfallClient()
    val magicSetRepository = MagicTheGatheringSetRepository(connection)
    val magicCardRepository = MagicTheGatheringCardRepository(connection)
    val cardRepository = CardRepository(connection)

    val magicSyncService = MagicTheGatheringSyncService(
        scryfallClient,
        magicSetRepository,
        cardRepository,
        magicCardRepository
    )

    magicSyncService.syncNewSetsAndCards()

    println("Magic The Gathering sync finished")

    val priceUpdateService = MagicTheGatheringPriceUpdateService(
        scryfallClient,
        magicCardRepository
    )

    val priceUpdateScheduler = MagicTheGatheringPriceUpdateScheduler(
        priceUpdateService
    )

    priceUpdateScheduler.start()

    val localPokemonDataSource = LocalPokemonDataSource()
    val tcgDexClient = TcgDexClient()
    val mapper = TcgDexCardMapper()
    val pokemonCardRepository = PokemonCardRepository(connection)

    val pokemonCardService = PokemonCardService(tcgDexClient, mapper, cardRepository, pokemonCardRepository)
    val pokemonSeeder = PokemonSeeder(pokemonCardService, localPokemonDataSource)

    pokemonSeeder.run()

    App(connection).start()

//    val mapper = TcgDexCardMapper()
//    val client = TcgDexClient()
//    val cardRepository = CardRepository(connection)
//    val pokemonCardRepository = PokemonCardRepository(connection)
//
//
//    val service = PokemonCardService (
//        client,
//        mapper,
//        cardRepository,
//        pokemonCardRepository
//    )
//
//    service.importCard(
//        "swsh3-136"
//    )

    connection.close()

    println("Connection closed")

}