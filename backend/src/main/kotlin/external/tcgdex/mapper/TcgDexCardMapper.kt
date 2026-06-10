package external.tcgdex.mapper

import external.tcgdex.dto.TcgDexCardDto
import models.Card
import models.PokemonCard
import models.enums.Rarity

class TcgDexCardMapper {

    fun toPokemonCard(dto: TcgDexCardDto, rawJson: String): PokemonCard {
        return PokemonCard(
            cardId = null,
            hp = dto.hp,
            rarity = mapRarity(dto.rarity),
            types = dto.types?.joinToString(", ") ?: "",
            evolvesFrom = dto.evolveFrom,
            setName = dto.set.name,
            setCode = dto.set.id,
            collectorNumber = dto.localId,
            artist = dto.illustrator,
            rawJson = rawJson
        )
    }

    private fun mapRarity(rarity: String?): Rarity {
        return when (rarity?.uppercase()) {
            "COMMON" -> Rarity.COMMON
            "UNCOMMON" -> Rarity.UNCOMMON
            "RARE" -> Rarity.RARE
            "ULTRA_RARE" -> Rarity.ULTRA_RARE
            "SECRET_RARE" -> Rarity.SECRET_RARE

            else -> {
                throw IllegalStateException("Unknown rarity: $rarity")
            }
        }
    }

    fun toCard(dto: TcgDexCardDto): Card {
        return Card(
            id = null,
            gameType = "Pokemon",
            externalApiId = dto.id,
            name = dto.name,
            imageUrl = dto.image
        )
    }
}