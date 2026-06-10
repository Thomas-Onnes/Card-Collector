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


fun main() {
    val connection = DatabaseConnection.getConnection()
    println("Database connected")

    try {
        val migrationRunner = MigrationRunner(connection)
        migrationRunner.run()
        if (dbSeed.equals("true")) {
            val databaseSeeder = DatabaseSeeder(connection)
            databaseSeeder.run()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("Card Collector Backend Running")
    App().start()
    println("Calling database for information")
    println(System.getProperty("user.dir"))
    println("Trying new API client")
    val mapper = TcgDexCardMapper()
    val client = TcgDexClient()
    val cardRepository = CardRepository(connection)
    val pokemonCardRepository = PokemonCardRepository(connection)


    val service = PokemonCardService (
        client,
        mapper,
        cardRepository,
        pokemonCardRepository
    )

    service.importCard(
        "swsh3-136"
    )

    connection.close()

}