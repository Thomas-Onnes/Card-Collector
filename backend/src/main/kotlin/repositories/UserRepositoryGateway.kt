package repositories

import models.User

interface UserRepositoryGateway {
    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

    fun createUser(user: User)
}