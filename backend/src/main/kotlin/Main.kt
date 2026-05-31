import core.App
import database.DatabaseConnection
import database.MigrationRunner

fun main() {
    val connection = DatabaseConnection.getConnection()
    println("Database connected")

    try {
        val migrationRunner = MigrationRunner(connection)
        migrationRunner.run()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("Card Collector Backend Running")
    App().start()

    connection.close()
}