package models

import java.math.BigDecimal

data class PokemonPriceUpdateCard(
    val cardId: Int,
    val externalApiId: String,
    val currentPrice: BigDecimal?
)