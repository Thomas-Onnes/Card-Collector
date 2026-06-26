package external.scryfall.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScryfallBulkDataDto(
    val id: String? = null,
    val type: String? = null,
    val name: String? = null,

    @JsonProperty("download_uri")
    val downloadUri: String
)
