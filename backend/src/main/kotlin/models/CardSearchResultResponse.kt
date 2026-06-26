package models

data class CardSearchResultResponse(
    val cardId: Int,
    val gameType: String,
    val name: String,
    val imageUrl: String?,
    val setName: String?,
    val setCode: String?,
    val collectorNumber: String?,
    val rarity: String?,
    val artistOrIllustrator: String?,
    val price: Double?
)