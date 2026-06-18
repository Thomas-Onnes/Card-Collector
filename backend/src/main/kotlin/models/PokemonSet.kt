package models

import java.sql.Date

data class PokemonSet (
    val id: Int?,
    val tcgDexId: Int,
    val name: String,
    val series: String,
    val releaseDate: Date
)