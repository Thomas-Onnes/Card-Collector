package dto.response

import models.enums.MagicTheGatheringRarity

data class MagicTheGatheringCardResponse(
    val name: String,
    val setName: String,
    val rarity: MagicTheGatheringRarity,
    val manaCost: String?,
    val typeLine: String?,
    val illustrator: String?,

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