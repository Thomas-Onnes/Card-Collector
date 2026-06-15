package external.scryfall.mapper

import external.scryfall.dto.ScryfallSetDto
import models.MagicTheGatheringSet

fun ScryfallSetDto.toMagicTheGatheringSet(): MagicTheGatheringSet {
    return MagicTheGatheringSet(
        scryfallId = id,
        code = code,
        name = name
    )
}