package database

import config.Environment
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {

    fun getConnection(
        maxRetries: Int = 10,
        retryDelayMillis: Long = 2_000
    ): Connection {
        try {
            Class.forName("org.postgresql.Driver")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(
                "PostgreSQL JDBC driver not found", e
            )
        }

        var lastException: SQLException? = null

        repeat(maxRetries) { attempt ->
            try {
                return DriverManager.getConnection(
                    Environment.dbUrl,
                    Environment.dbUser,
                    Environment.dbPassword
                )
            } catch (e: SQLException) {
                lastException = e

                val currentAttempt = attempt + 1

                if (currentAttempt < maxRetries) {
                    println(
                        "Database is not ready yet " +
                                "($currentAttempt/$maxRetries). Retrying..."
                    )

                    Thread.sleep(retryDelayMillis)
                }
            }
        }

        throw RuntimeException(
            "Failed to connect to database", lastException
        )
    }
}
