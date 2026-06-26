package external.tcgdex.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexSetDto(
    val id: String,
    val name: String,
    val serie: TcgDexSeriesDto? = null,
    val releaseDate: String? = null,
    val cards: List<TcgDexCardSummaryDto> = emptyList()
)
