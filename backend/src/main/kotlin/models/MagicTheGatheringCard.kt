package models

import models.enums.MagicTheGatheringRarity
import java.math.BigDecimal

data class MagicTheGatheringCard (
    val id: Int?,
    val scryfallId: String,
    val name: String,
    val setCode: String,
    val setName: String,
    val rarity: MagicTheGatheringRarity,
    val manaCost: String?,
    val typeLine: String?,
    val illustrator: String?,
    val priceEur: BigDecimal?,

    val isCreature: Boolean,
    val isInstant: Boolean,
    val isSorcery: Boolean,
    val isEnchantment: Boolean,
    val isArtifact: Boolean,
    val isLand: Boolean,
    val isPlaneswalker: Boolean,
    val isLegendary: Boolean,
    val isSaga: Boolean,
    val isRoom: Boolean,
    val isBattle: Boolean,
    val isKindred: Boolean
)