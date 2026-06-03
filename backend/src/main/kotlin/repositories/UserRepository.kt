package repositories

import models.User
import java.sql.Connection

class UserRepository (
    private val databaseConnection: Connection
) {
    private val findAllQuery = "SELECT * FROM users"

    fun findAll(): List<User> {
        val statement = databaseConnection.createStatement()
        val result = statement.executeQuery(findAllQuery)
        val users = arrayListOf<User>()
        while(result.next()) {
            val user = User(
                result.getInt("id"),
                result.getString("username"),
                result.getString("email"),
                result.getString("password_hash"),
                result.getTimestamp("created_at").toLocalDateTime()
            )
            users.add(user)
        }
        return users
    }
}