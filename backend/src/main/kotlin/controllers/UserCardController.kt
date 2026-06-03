package controllers

import services.UserCardService

class UserCardController(
    private val userCardService: UserCardService
) {

    fun getUserCards(): String {
        return userCardService.getAllUserCards().toString()
    }
}