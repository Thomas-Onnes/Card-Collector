package external.tcgdex.dto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Date

@JsonIgnoreProperties(ignoreUnknown = true)
data class TcgDexSetDto(
    val id: Int,
    val name: String,
    val series: String,
    val releaseDate: Date
)