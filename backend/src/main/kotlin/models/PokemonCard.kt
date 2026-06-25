package models

import models.enums.PokemonRarity
import java.math.BigDecimal

data class PokemonCard (
    val cardId: Int?,
    val setId: Int,
    val hp: Int?,
    val rarity: PokemonRarity,
    val types: String,
    val evolvesFrom: String?,
    val collectorNumber: String,
    val artist: String?,
    val priceEur: BigDecimal?,
    val rawJson: String?
)