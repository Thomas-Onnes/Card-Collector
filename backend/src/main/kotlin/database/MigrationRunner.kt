package database

import java.sql.Connection

object MigrationRunner {

    private val migrationFiles = listOf(
        "V1_create_users_table.sql",
        "V2_create_user_collections.sql",
        "V3_create_cards_table.sql",
        "V4_collection_cards_table.sql",
        "V5_create_pokemon_cards.sql",
        "V6_create_magic_the_gathering_cards.sql",
        "V7_create_magic_the_gathering_sets_table.sql"
    )

    fun run(connection: Connection) {
        println("Running database migrations")

        for (fileName in migrationFiles) {
            println("Running migration: $fileName")

            val sql = readResourceFile("database/migrations/$fileName")

            connection.createStatement().use { statement ->
                statement.execute(sql)
            }

            println("Migration finished: $fileName")
        }

        println("All migrations finished")
    }

    private fun readResourceFile(path: String): String {
        return Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("Resource not found: $path")
    }
}