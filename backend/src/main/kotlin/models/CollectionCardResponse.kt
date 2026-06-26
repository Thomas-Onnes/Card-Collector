package models

data class CollectionCardResponse(
    val collectionCardId: Int,
    val cardId: Int,
    val name: String,
    val gameType: String,
    val imageUrl: String?,
    val quantity: Int,
    val cardCondition: String,
    val isFoil: Boolean,
    val language: String,
    val setName: String?,
    val setCode: String?,
    val collectorNumber: String?,
    val rarity: String?,
    val artistOrIllustrator: String?,
    val price: Double?
)