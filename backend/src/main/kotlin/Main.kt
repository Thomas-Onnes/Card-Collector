import config.Environment
import core.App
import database.DatabaseConnection
import database.MigrationRunner

fun main() {
    val connection = DatabaseConnection.getConnection()
    println("Database connected")

    val migrationRunner = MigrationRunner()
    migrationRunner.run()

    println("Card Collector Backend Running")
    App().start()

    connection.close()
}