package external.tcgdex.mapper

import external.tcgdex.dto.TcgDexCardDto
import models.Card
import models.PokemonCard
import models.enums.PokemonRarity

class TcgDexCardMapper {

    fun toPokemonCard(
        dto: TcgDexCardDto,
        rawJson: String,
        generatedCardId: Int,
        generatedSetId: Int
    ): PokemonCard {
        return PokemonCard(
            cardId = generatedCardId,
            setId = generatedSetId,
            hp = dto.hp,
            rarity = PokemonRarity.fromApiValue(dto.rarity),
            types = dto.types?.joinToString(", ") ?: "Unknown",
            evolvesFrom = dto.evolveFrom,
            collectorNumber = dto.localId,
            artist = dto.illustrator,
            priceEur = dto.pricing?.cardMarket?.avg,
            rawJson = rawJson
        )
    }

    fun toCard(dto: TcgDexCardDto): Card {
        return Card(
            id = null,
            gameType = "pokemon",
            externalApiId = dto.id,
            name = dto.name,
            imageUrl = dto.image
        )
    }
}
