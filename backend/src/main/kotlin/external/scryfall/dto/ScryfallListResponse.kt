package external.scryfall.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallListResponse<T>(
    val data: List<T>,

    @JsonProperty("has_more")
    val hasMore: Boolean? = null,

    @JsonProperty("next_page")
    val nextPage: String? = null
)
