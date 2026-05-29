package database

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class MigrationRunner {
    private val migrationPath = "backend/src/main/resources/database/migrations"

    fun run() {
        val migrations = getMigrationFiles()
        println(readMigrationFile(migrations[0]))
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
}