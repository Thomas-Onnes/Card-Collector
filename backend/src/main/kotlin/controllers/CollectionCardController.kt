package controllers

import services.CollectionCardService

class CollectionCardController(
    private val collectionCardService: CollectionCardService
) {

    fun getCollectionCards(): String {
        return collectionCardService.getAllCollectionCards().toString()
    }
}