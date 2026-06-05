package external.tcgdex.dto

data class TcgDexCardDto (
    val id: String,
    val name: String,
    val hp: Int?,
    val rarity: String?,
    val types: List<String>?,
    val evolveFrom: String?,
    val illustrator: String?,
    val localId: String,
    val image: String?,
    val st: TcgDexSetDto
)