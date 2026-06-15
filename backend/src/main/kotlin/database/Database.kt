package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

object Database {

    private val dataSource: HikariDataSource by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DB_URL")
                ?: "jdbc:postgresql://localhost:5432/card_collector"

            username = System.getenv("DB_USER") ?: "admin"
            password = System.getenv("DB_PASSWORD") ?: "ccDB1!"

            maximumPoolSize = 5
            minimumIdle = 1
            connectionTimeout = 5_000
            idleTimeout = 30_000
            maxLifetime = 300_000
        }

        HikariDataSource(config)
    }

    fun connect(): Connection {
        return dataSource.connection
    }

    fun close() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }
}