package controllers

import services.CardService

class CardController(
    private val cardService: CardService
) {

    fun getCards(): String {
        return cardService.getAllCards().toString()
    }
}