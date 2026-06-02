package database

import java.sql.Connection
import java.sql.DriverManager

object Database {

    private const val URL =
        "jdbc:postgresql://database:5432/card_collector"

    private const val USER = "admin"
    private const val PASSWORD = "ccDB1!"

    fun connect(): Connection {

        return DriverManager.getConnection(
            URL,
            USER,
            PASSWORD
        )
    }
}