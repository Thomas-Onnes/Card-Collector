package services

import models.UserCard
import repositories.UserCardRepository

class UserCardService(
    private val repository: UserCardRepository
) {

    fun getAllUserCards(): List<UserCard> {
        return repository.findAll()
    }
}