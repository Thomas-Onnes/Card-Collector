package external.tcgdex.dto

import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexCardMarketDto (
    val avg: BigDecimal?,
    val low: BigDecimal?
)