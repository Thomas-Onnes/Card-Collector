package controllers

import services.UserCollectionService

class UserCollectionController (
    private val userCollectionService: UserCollectionService
) {

    fun getUserCollections(): String {
        return userCollectionService.getAllUserCollection().toString()
    }
}