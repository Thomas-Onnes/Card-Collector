package external.tcgdex.mapper

import external.tcgdex.dto.TcgDexSetResponse
import models.PokemonSet
import java.sql.Date

class TcgDexSetMapper {

    fun toPokemonSet(response: TcgDexSetResponse): PokemonSet {
        val dto = response.dto

        return PokemonSet(
            id = null,
            tcgDexId = dto.id,
            name = dto.name,
            series = dto.serie.name,
            releaseDate = Date.valueOf(dto.releaseDate)
        )
    }
}