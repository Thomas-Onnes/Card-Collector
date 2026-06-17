package external.scryfall.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallSetDto(
    val id: String,
    val code: String,
    val name: String,

    @JsonProperty("card_count")
    val cardCount: Int? = null,

    @JsonProperty("search_uri")
    val searchUri: String? = null,
)
