package models

import models.enums.PokemonRarity

data class PokemonCard (
    val cardId: Int?,
    val setId: Int?,
    val hp: Int?,
    val rarity: PokemonRarity,
    val types: String,
    val evolvesFrom: String?,
    val collectorNumber: String,
    val artist: String?,
    val rawJson: String?
)