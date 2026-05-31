package database

import java.nio.file.Path
import java.sql.Connection
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class MigrationRunner(
    private val databaseConnection: Connection
) {
    private val migrationPath = "backend/src/main/resources/database/migrations"

    fun run() {
        val migrations = getMigrationFiles()
        println("All the files are gathered")

        for (migration in migrations) {
            println("Running: ${migration.fileName}")
            try {
                val sql = readMigrationFile(migration)
                executeMigration(sql)
                println("Migration succescful")
            } catch (e: Exception) {
                if (e.message?.contains("already exists") == true) {
                    println("Migration skipped: table already exists")
                } else {
                    throw e
                }
            }
        }
    }

    private fun getMigrationFiles(): List<Path> {
        val migrationDirectory =
            Path.of(migrationPath)

        return migrationDirectory
            .listDirectoryEntries("*.sql")
            .sortedBy { it.fileName.toString() }
    }

    private fun readMigrationFile(path: Path): String {
        return path.readText()
    }

    private fun executeMigration(sql: String) {
        val statement = databaseConnection.createStatement()
        statement.executeUpdate(sql)
        statement.close()
    }
}