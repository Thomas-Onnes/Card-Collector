package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexCardSummaryDto(
    val id: String
)
