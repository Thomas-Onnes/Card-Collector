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
            series = dto.serie?.name ?: "Unknown",
            releaseDate = parseReleaseDate(dto.releaseDate)
        )
    }

    private fun parseReleaseDate(value: String?): Date {
        return try {
            Date.valueOf(value ?: "1970-01-01")
        } catch (e: IllegalArgumentException) {
            Date.valueOf("1970-01-01")
        }
    }
}
