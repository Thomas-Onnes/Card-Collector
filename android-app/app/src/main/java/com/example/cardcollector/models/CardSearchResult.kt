package com.example.cardcollector.models

data class CardSearchResult(
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
) {
    fun displayLabel(): String {
        val details = mutableListOf<String>()

        if (!setName.isNullOrBlank()) {
            details.add(setName)
        }

        if (!setCode.isNullOrBlank()) {
            details.add(setCode)
        }

        if (!collectorNumber.isNullOrBlank()) {
            details.add("#$collectorNumber")
        }

        if (!rarity.isNullOrBlank()) {
            details.add(rarity)
        }

        if (!artistOrIllustrator.isNullOrBlank()) {
            details.add(artistOrIllustrator)
        }

        return if (details.isEmpty()) {
            name
        } else {
            "$name (${details.joinToString(" - ")})"
        }
    }
}