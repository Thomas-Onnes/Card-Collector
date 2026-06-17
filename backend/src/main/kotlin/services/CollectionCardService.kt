package services

import models.CollectionCard
import repositories.CollectionCardRepository

class CollectionCardService(
    private val repository: CollectionCardRepository
) {

    fun getAllCollectionCards(): List<CollectionCard> {
        return repository.findAll()
    }
}