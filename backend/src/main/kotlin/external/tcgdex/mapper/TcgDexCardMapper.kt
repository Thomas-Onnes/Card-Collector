package external.tcgdex.mapper

import external.tcgdex.dto.TcgDexCardDto
import external.tcgdex.dto.TcgDexSetDto
import models.Card
import models.PokemonCard
import models.PokemonSet
import models.enums.PokemonRarity

class TcgDexCardMapper {

    fun toPokemonCard(dto: TcgDexCardDto, rawJson: String): PokemonCard {
        return PokemonCard(
            cardId = null,
            setId = dto.set.id,
            hp = dto.hp,
            rarity = mapRarity(dto.rarity),
            types = dto.types?.joinToString(", ") ?: "",
            evolvesFrom = dto.evolveFrom,
            collectorNumber = dto.localId,
            artist = dto.illustrator,
            rawJson = rawJson
        )
    }

    private fun mapRarity(rarity: String?): PokemonRarity {
        return when (rarity?.uppercase()) {
            "COMMON" -> PokemonRarity.COMMON
            "UNCOMMON" -> PokemonRarity.UNCOMMON
            "RARE" -> PokemonRarity.RARE
            "ULTRA_RARE" -> PokemonRarity.ULTRA_RARE
            "SECRET_RARE" -> PokemonRarity.SECRET_RARE

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

    fun toSet(dto: TcgDexSetDto): PokemonSet {
        return PokemonSet(
            id = null,
            tcgDexId = dto.id,
            name = dto.name,
            series = dto.series,
            releaseDate = dto.releaseDate
        )
    }
}