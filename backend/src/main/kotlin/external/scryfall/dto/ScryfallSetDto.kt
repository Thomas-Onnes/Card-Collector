package external.scryfall.dto
import com.fasterxml.jackson.annotation.JsonProperty

data class ScryfallSetDto (
    val id: String,
    val code: String,
    val name: String,
)