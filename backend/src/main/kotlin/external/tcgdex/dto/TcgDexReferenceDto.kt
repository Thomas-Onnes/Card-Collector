package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexReferenceDto(
    val id: String,
    val name: String
)
