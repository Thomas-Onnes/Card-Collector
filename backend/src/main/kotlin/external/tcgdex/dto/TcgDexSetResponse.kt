package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexSetResponse (
    val dto: TcgDexSetDto,
    val rawJson: String
)