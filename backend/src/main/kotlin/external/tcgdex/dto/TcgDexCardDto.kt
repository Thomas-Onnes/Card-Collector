package external.tcgdex.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexCardDto(
    val id: String,
    val name: String,
    val hp: Int? = null,
    val rarity: String? = null,
    val types: List<String>? = null,
    val evolveFrom: String? = null,
    val illustrator: String? = null,
    val localId: String,
    val image: String? = null,
    val set: TcgDexReferenceDto? = null,
    val pricing: TcgDexPricingDto? = null
)
