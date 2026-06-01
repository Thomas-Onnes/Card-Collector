package database

import java.nio.file.Path
import java.sql.Connection
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class DatabaseSeeder(
    private val databaseConnection: Connection
) {
    private val seedPath = "backend/src/main/resources/database/seeds"

    fun run() {
        val seeds = getSeedFiles()
        println("All the files are gathered")

        for (seed in seeds) {
            println("Running: ${seed.fileName}")
            try {
                val sql = readSeedFiles(seed)
                executeSeed(sql)
                println("Seed succesful")
            } catch (e: Exception) {
                    throw e
                }
            }
        }

    private fun getSeedFiles(): List<Path> {
        val migrationDirectory =
            Path.of(seedPath)

        return migrationDirectory
            .listDirectoryEntries("*.sql")
            .sortedBy { it.fileName.toString() }
    }

    private fun readSeedFiles(path: Path): String {
        return path.readText()
    }

    private fun executeSeed(sql: String) {
        val statement = databaseConnection.createStatement()
        statement.executeUpdate(sql)
        statement.close()
    }
}