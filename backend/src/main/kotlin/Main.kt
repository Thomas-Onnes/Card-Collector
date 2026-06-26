import api.AuthHandler
import api.CardsHandler
import api.CollectionsHandler
import api.LoginHandler
import api.LogoutHandler
import com.sun.net.httpserver.HttpServer
import database.Database
import database.DatabaseSeeder
import database.MigrationRunner
import java.net.InetSocketAddress
import java.util.concurrent.Executors

fun main() {
    waitForDatabase()

    val runMigrations = envFlag("RUN_MIGRATIONS", defaultValue = true)
    val seedDatabase = envFlag("SEED_DATABASE", defaultValue = false)

    println("RUN_MIGRATIONS=$runMigrations")
    println("SEED_DATABASE=$seedDatabase")

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

    val server = HttpServer.create(InetSocketAddress(8080), 0)

    server.createContext("/register", AuthHandler())
    server.createContext("/login", LoginHandler())
    server.createContext("/logout", LogoutHandler())
    server.createContext("/collections", CollectionsHandler())
    server.createContext("/cards", CardsHandler())

    server.executor = Executors.newFixedThreadPool(8)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(0)
            Database.close()
        }
    )

    server.start()

    println("Server running on port 8080")
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