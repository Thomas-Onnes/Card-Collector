package models

data class CollectionCardsResponse(
    val cards: List<CollectionCardResponse>,
    val totalPrice: Double
)