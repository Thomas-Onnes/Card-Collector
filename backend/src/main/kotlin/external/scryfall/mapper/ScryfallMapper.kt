package external.scryfall.mapper

import external.scryfall.dto.ScryfallCardDto
import models.MagicTheGatheringCard
import models.enums.MagicTheGatheringRarity

fun ScryfallCardDto.toMagicTheGatheringCard(): MagicTheGatheringCard {
    val type = typeLine.orEmpty().lowercase()

    return MagicTheGatheringCard(
        id = null,
        scryfallId = id,
        name = name,
        setCode = setCode,
        setName = setName,
        rarity = mapMagicRarity(rarity),
        manaCost = manaCost,
        typeLine = typeLine,
        illustrator = illustrator,
        priceEur = prices?.eur?.toBigDecimalOrNull(),
        isCreature = type.contains("creature"),
        isInstant = type.contains("instant"),
        isSorcery = type.contains("sorcery"),
        isEnchantment = type.contains("enchantment"),
        isArtifact = type.contains("artifact"),
        isLand = type.contains("land"),
        isPlaneswalker = type.contains("planeswalker"),
        isLegendary = type.contains("legendary"),
        isSaga = type.contains("saga"),
        isRoom = type.contains("room"),
        isBattle = type.contains("battle"),
        isKindred = type.contains("kindred")
    )
}

private fun mapMagicRarity(rarity: String): MagicTheGatheringRarity {
    return when (rarity.trim().uppercase()) {
        "COMMON" -> MagicTheGatheringRarity.COMMON
        "UNCOMMON" -> MagicTheGatheringRarity.UNCOMMON
        "RARE" -> MagicTheGatheringRarity.RARE
        "MYTHIC" -> MagicTheGatheringRarity.MYTHIC
        "SPECIAL" -> MagicTheGatheringRarity.SPECIAL
        "BONUS" -> MagicTheGatheringRarity.BONUS
        else -> MagicTheGatheringRarity.UNKNOWN
    }
}
