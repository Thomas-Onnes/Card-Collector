package mapper
import models.MagicTheGatheringCard
import dto.response.MagicTheGatheringCardResponse

fun MagicTheGatheringCard.toResponse(): MagicTheGatheringCardResponse {
    return MagicTheGatheringCardResponse(
        name = name,
        setName = setName,
        rarity = rarity,
        manaCost = manaCost,
        typeLine = typeLine,
        illustrator = illustrator,

        isCreature = isCreature,
        isInstant = isInstant,
        isSorcery = isSorcery,
        isEnchantment = isEnchantment,
        isArtifact = isArtifact,
        isLand = isLand,
        isPlaneswalker = isPlaneswalker,
        isLegendary = isLegendary,
        isSaga = isSaga,
        isRoom = isRoom,
        isBattle = isBattle,
        isKindred = isKindred
    )
}