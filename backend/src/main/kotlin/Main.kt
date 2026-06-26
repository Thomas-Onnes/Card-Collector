import api.AuthHandler
import api.CardsHandler
import api.CollectionsHandler
import api.LoginHandler
import api.LogoutHandler
import com.sun.net.httpserver.HttpServer
import database.Database
import database.DatabaseSeeder
import database.MigrationRunner
import services.BulkCardImportScheduler
import services.BulkCardImportService
import java.net.InetSocketAddress
import java.util.concurrent.Executors

fun main() {
    waitForDatabase()

    val runMigrations = envFlag("RUN_MIGRATIONS", defaultValue = true)
    val seedDatabase = envFlag("SEED_DATABASE", defaultValue = false)

    val bulkImportOnStartup = envFlag("BULK_IMPORT_ON_STARTUP", defaultValue = true)
    val forceBulkImport = envFlag("FORCE_BULK_IMPORT", defaultValue = false)
    val importPokemonBulk = envFlag("IMPORT_POKEMON_BULK", defaultValue = true)
    val importMagicBulk = envFlag("IMPORT_MAGIC_BULK", defaultValue = true)

    val autoPriceUpdate = envFlag("AUTO_PRICE_UPDATE", defaultValue = true)
    val updatePokemonPrices = envFlag("UPDATE_POKEMON_PRICES", defaultValue = true)
    val updateMagicPrices = envFlag("UPDATE_MAGIC_PRICES", defaultValue = true)

    val pokemonMaxCards = envInt("POKEMON_BULK_MAX_CARDS", defaultValue = 0)
    val magicMaxCards = envInt("MTG_BULK_MAX_CARDS", defaultValue = 0)
    val bulkRequestDelayMillis = envLong("BULK_IMPORT_REQUEST_DELAY_MS", defaultValue = 0L)

    val priceUpdateInitialDelayMinutes = envLong(
        name = "PRICE_UPDATE_INITIAL_DELAY_MINUTES",
        defaultValue = 1440L
    )

    val priceUpdateIntervalMinutes = envLong(
        name = "PRICE_UPDATE_INTERVAL_MINUTES",
        defaultValue = 1440L
    )

    println("RUN_MIGRATIONS=$runMigrations")
    println("SEED_DATABASE=$seedDatabase")
    println("BULK_IMPORT_ON_STARTUP=$bulkImportOnStartup")
    println("FORCE_BULK_IMPORT=$forceBulkImport")
    println("IMPORT_POKEMON_BULK=$importPokemonBulk")
    println("IMPORT_MAGIC_BULK=$importMagicBulk")
    println("AUTO_PRICE_UPDATE=$autoPriceUpdate")
    println("UPDATE_POKEMON_PRICES=$updatePokemonPrices")
    println("UPDATE_MAGIC_PRICES=$updateMagicPrices")
    println("POKEMON_BULK_MAX_CARDS=$pokemonMaxCards")
    println("MTG_BULK_MAX_CARDS=$magicMaxCards")
    println("BULK_IMPORT_REQUEST_DELAY_MS=$bulkRequestDelayMillis")

    Database.connect().use { connection ->
        if (runMigrations) {
            MigrationRunner.run(connection)
        } else {
            println("Database migrations disabled")
        }

        if (seedDatabase) {
            DatabaseSeeder.run(connection)
        } else {
            println("Database seeding disabled")
        }
    }

    if (bulkImportOnStartup) {
        runStartupBulkImport(
            importPokemonBulk = importPokemonBulk,
            importMagicBulk = importMagicBulk,
            forceBulkImport = forceBulkImport,
            pokemonMaxCards = pokemonMaxCards,
            magicMaxCards = magicMaxCards,
            bulkRequestDelayMillis = bulkRequestDelayMillis
        )
    } else {
        println("Startup bulk import disabled")
    }

    val bulkPriceUpdateScheduler =
        if (autoPriceUpdate) {
            BulkCardImportScheduler(
                updatePokemon = updatePokemonPrices,
                updateMagic = updateMagicPrices,
                pokemonMaxCards = pokemonMaxCards,
                magicMaxCards = magicMaxCards,
                requestDelayMillis = bulkRequestDelayMillis
            ).also { scheduler ->
                scheduler.start(
                    initialDelayMinutes = priceUpdateInitialDelayMinutes,
                    intervalMinutes = priceUpdateIntervalMinutes
                )
            }
        } else {
            println("Automatic price update disabled")
            null
        }

    val server = HttpServer.create(InetSocketAddress(8080), 0)

    server.createContext("/register", AuthHandler())
    server.createContext("/login", LoginHandler())
    server.createContext("/logout", LogoutHandler())
    server.createContext("/collections", CollectionsHandler())
    server.createContext("/cards", CardsHandler())

    server.executor = Executors.newFixedThreadPool(8)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            bulkPriceUpdateScheduler?.stop()
            server.stop(0)
            Database.close()
        }
    )

    server.start()

    println("Server running on port 8080")
}

private fun runStartupBulkImport(
    importPokemonBulk: Boolean,
    importMagicBulk: Boolean,
    forceBulkImport: Boolean,
    pokemonMaxCards: Int,
    magicMaxCards: Int,
    bulkRequestDelayMillis: Long
) {
    try {
        Database.connect().use { connection ->
            val result = BulkCardImportService(connection).runStartupBulkImport(
                importPokemon = importPokemonBulk,
                importMagic = importMagicBulk,
                forceImport = forceBulkImport,
                pokemonMaxCards = pokemonMaxCards,
                magicMaxCards = magicMaxCards,
                requestDelayMillis = bulkRequestDelayMillis
            )

            println(
                "Startup bulk import finished. " +
                    "Pokemon: ${result.importedPokemonCards}, Magic: ${result.importedMagicCards}"
            )
        }
    } catch (e: Exception) {
        println("Startup bulk import failed: ${e::class.java.simpleName}: ${e.message}")
    }
}

private fun envFlag(
    name: String,
    defaultValue: Boolean
): Boolean {
    return when (System.getenv(name)?.trim()?.lowercase()) {
        "true", "1", "yes", "y", "on" -> true
        "false", "0", "no", "n", "off" -> false
        null, "" -> defaultValue
        else -> defaultValue
    }
}

private fun envInt(
    name: String,
    defaultValue: Int
): Int {
    val value = System.getenv(name)?.trim()

    if (value.isNullOrBlank()) {
        return defaultValue
    }

    return value.toIntOrNull() ?: defaultValue
}

private fun envLong(
    name: String,
    defaultValue: Long
): Long {
    val value = System.getenv(name)?.trim()

    if (value.isNullOrBlank()) {
        return defaultValue
    }

    return value.toLongOrNull() ?: defaultValue
}

private fun waitForDatabase() {
    val maxAttempts = 20

    for (attempt in 1..maxAttempts) {
        try {
            Database.connect().use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT 1").use { resultSet ->
                        if (resultSet.next()) {
                            println("Database connection ready")
                            return
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Waiting for database... attempt $attempt/$maxAttempts")
            Thread.sleep(2000)
        }
    }

    throw IllegalStateException("Database was not ready after $maxAttempts attempts")
}
