package repositories

import database.Database
import models.User

class UserRepository : UserRepositoryGateway {

    override fun findByEmail(email: String): User? {
        val sql = "SELECT id, username, email, password_hash FROM users WHERE email = ?"

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, email)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return User(
                            id = resultSet.getInt("id"),
                            username = resultSet.getString("username"),
                            email = resultSet.getString("email"),
                            passwordHashed = resultSet.getString("password_hash")
                        )
                    }
                }
            }
        }

        return null
    }

    override fun findByUsername(username: String): User? {
        val sql = "SELECT id, username, email, password_hash FROM users WHERE username = ?"

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, username)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        return User(
                            id = resultSet.getInt("id"),
                            username = resultSet.getString("username"),
                            email = resultSet.getString("email"),
                            passwordHashed = resultSet.getString("password_hash")
                        )
                    }
                }
            }
        }

        return null
    }

    override fun createUser(user: User) {
        val sql = """
            INSERT INTO users
            (username, email, password_hash)
            VALUES (?, ?, ?)
        """.trimIndent()

        Database.connect().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, user.username)
                statement.setString(2, user.email)
                statement.setString(3, user.passwordHashed)
                statement.executeUpdate()
            }
        }
    }
}