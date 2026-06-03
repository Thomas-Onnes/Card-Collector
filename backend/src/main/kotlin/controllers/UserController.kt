package controllers

import services.UserService

class UserController(
    private val userService: UserService
) {

    fun getUsers(): String {
        return userService.getAllUsers().toString()
    }
}