package services

import models.User
import repositories.UserRepository

class UserService(
    private val repository: UserRepository
) {

    fun getAllUsers(): List<User> {
        return repository.findAll()
    }
}