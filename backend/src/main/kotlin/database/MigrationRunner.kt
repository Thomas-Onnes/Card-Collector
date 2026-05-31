package database

import java.nio.file.Path
import java.sql.Connection
import java.sql.Statement
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
            val sql = readMigrationFile(migration)
            println("SQL: $sql")
            executeMigration(sql)
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