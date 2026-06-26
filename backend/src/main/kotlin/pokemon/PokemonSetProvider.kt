package pokemon

import config.Environment
import external.tcgdex.TcgDexClient
import external.tcgdex.dto.TcgDexSetSummaryDto

class PokemonSetProvider(
    private val tcgDexClient: TcgDexClient
) {

    fun getSetsToImport(): List<TcgDexSetSummaryDto> {

        return if (Environment.importAllPokemonSets) {
            tcgDexClient.getAllSets()
        } else {
            getSelectedSets()
        }
    }

    private fun getSelectedSets(): List<TcgDexSetSummaryDto> {
        val selectedSets = Environment.selectedPokemonSets
        println(selectedSets)
        return selectedSets.map { setId ->
            TcgDexSetSummaryDto(
                id = setId,
                name = ""
            )
        }
    }
}