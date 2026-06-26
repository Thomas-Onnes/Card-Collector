package external.scryfall.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallCardDto (
    val id: String, // this is the Scryfall ID
    val name: String,

    @JsonProperty("set")
    val setCode: String,

    @JsonProperty("set_name")
    val setName: String,

    val rarity: String,

    @JsonProperty("mana_cost")
    val manaCost: String?,

    @JsonProperty("type_line")
    val typeLine: String?,

    @JsonProperty("artist")
    val illustrator: String?,

    val prices: ScryfallPricesDto?
)
