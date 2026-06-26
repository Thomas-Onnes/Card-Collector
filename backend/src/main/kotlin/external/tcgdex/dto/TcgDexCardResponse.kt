package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexCardResponse (
    val dto: TcgDexCardDto,
    val rawJson: String
)