package pokemon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import external.tcgdex.dto.TcgDexCardDto
import external.tcgdex.dto.TcgDexSetDto

class LocalPokemonDataSource {

    private val objectMapper = jacksonObjectMapper()

    fun getSet():
        List<TcgDexSetDto> {

        val inputStream = javaClass.getResourceAsStream("/pokemon/sets.json")

        val json = inputStream
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalStateException(
                "sets.json not found"
            )

        return objectMapper.readValue<List<TcgDexSetDto>>(json)
    }

    fun getSetCards(setId: String): List<TcgDexCardDto> {
        val inputStream = javaClass.getResourceAsStream("/pokemon/${setId}_cards.json")

        val json = inputStream
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalStateException(
                "cards.json not found"
            )

        return objectMapper.readValue<List<TcgDexCardDto>>(json)
    }
}
