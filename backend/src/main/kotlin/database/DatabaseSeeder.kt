package database

import java.sql.Connection

object DatabaseSeeder {

    private val seedFiles = listOf(
        "001_seed_users.sql",
        "002_seed_user_collections.sql",
        "003_seed_cards.sql",
        "004_seed_collection_cards.sql",
        "005_seed_pokemon_cards.sql",
        "006_seed_magic_the_gathering_cards.sql"
    )

    fun run(connection: Connection) {
        println("Running database seeders")

        for (fileName in seedFiles) {
            println("Running seed: $fileName")

            val sql = readResourceFile("database/seeds/$fileName")

            if (sql.isBlank()) {
                println("Seed skipped because file is empty: $fileName")
                continue
            }

            connection.createStatement().use { statement ->
                statement.execute(sql)
            }

            println("Seed finished: $fileName")
        }

        println("All seeders finished")
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