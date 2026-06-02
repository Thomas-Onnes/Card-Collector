package repositories

import database.Database
import models.User

class UserRepository {

    fun findByEmail(email: String): User? {

        val sql =
            "SELECT * FROM users WHERE email = ?"

        Database.connect().use { conn ->

            val stmt = conn.prepareStatement(sql)

            stmt.setString(1, email)

            val rs = stmt.executeQuery()

            if (rs.next()) {

                return User(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    passwordHashed = rs.getString("password_hashed")
                )
            }
        }

        return null
    }

    fun findByUsername(username: String): User? {

        val sql =
            "SELECT * FROM users WHERE username = ?"

        Database.connect().use { conn ->

            val stmt = conn.prepareStatement(sql)

            stmt.setString(1, username)

            val rs = stmt.executeQuery()

            if (rs.next()) {

                return User(
                    id = rs.getInt("id"),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    passwordHashed = rs.getString("password_hashed")
                )
            }
        }

        return null
    }

    fun createUser(user: User) {

        val sql = """
            INSERT INTO users
            (username, email, password_hashed)
            VALUES (?, ?, ?)
        """

        Database.connect().use { conn ->

            val stmt = conn.prepareStatement(sql)

            stmt.setString(1, user.username)
            stmt.setString(2, user.email)
            stmt.setString(3, user.passwordHashed)

            stmt.executeUpdate()
        }
    }
}