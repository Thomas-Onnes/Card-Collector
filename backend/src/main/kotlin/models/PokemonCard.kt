package models

import models.enums.Rarity

data class PokemonCard (
    val cardId: Int?,
    val hp: Int?,
    val rarity: Rarity,
    val types: String,
    val evolvesFrom: String?,
    val setName: String,
    val setCode: String,
    val collectorNumber: String,
    val artist: String?,
    val rawJson: String?
)