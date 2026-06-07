package database

import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val URL = System.getenv("DB_URL")

    private val USER = System.getenv("DB_USER")

    private val PASSWORD = System.getenv("DB_PASSWORD")

    fun connect(): Connection {
        return DriverManager.getConnection(URL, USER, PASSWORD)
    }
}