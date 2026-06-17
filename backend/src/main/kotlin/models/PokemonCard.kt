package models

import models.enums.PokemonRarity

data class PokemonCard (
    val cardId: Int?,
    val hp: Int?,
    val rarity: PokemonRarity,
    val types: String,
    val evolvesFrom: String?,
    val setName: String,
    val setCode: String,
    val collectorNumber: String,
    val artist: String?,
    val rawJson: String?
)