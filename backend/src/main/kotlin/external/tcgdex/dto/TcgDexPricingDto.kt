package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexPricingDto (
    @JsonProperty("cardmarket")
    val cardMarket: TcgDexCardMarketDto?
)