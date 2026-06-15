package services

import models.UserCollection
import repositories.UserCollectionRepository

class UserCollectionService(
    private val repository: UserCollectionRepository
) {

    fun getAllUserCollection(): List<UserCollection> {
        return repository.findAll()
    }
}