import config.Environment.dbSeed
import core.App
import database.DatabaseConnection
import database.DatabaseSeeder
import database.MigrationRunner

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
    connection.close()
    println(System.getProperty("user.dir"))
}