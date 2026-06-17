package database

import config.Environment
import java.sql.Connection
import java.sql.SQLException
import java.sql.DriverManager

object DatabaseConnection {

    fun getConnection(): Connection {
        return try {

            Class.forName("org.postgresql.Driver")

            DriverManager.getConnection(
                Environment.dbUrl,
                Environment.dbUser,
                Environment.dbPassword
            )
        } catch (e: SQLException) {

            throw RuntimeException(
            "Failed to connect to database", e)

        } catch (e: ClassNotFoundException) {

            throw RuntimeException(
                "PostgreSQL JDBC driver not found", e)
        }
    }
}