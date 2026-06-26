package models

data class AddCardToCollectionRequest(
    val cardId: Int,
    val quantity: Int,
    val cardCondition: String,
    val isFoil: Boolean,
    val language: String
)