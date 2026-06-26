package pokemon.import

import config.Environment.importAllPokemonSets
import config.Environment.selectedPokemonSets
import external.tcgdex.TcgDexClient
import external.tcgdex.dto.TcgDexSetSummaryDto

class PokemonSetProvider(
    private val tcgDexClient: TcgDexClient
) {

    fun getSetsToImport(): List<TcgDexSetSummaryDto> {

        return if (importAllPokemonSets) {
            tcgDexClient.getAllSets()
        } else {
            getSelectedSets()
        }
    }

    private fun getSelectedSets(): List<TcgDexSetSummaryDto> {
        val selectedSets = selectedPokemonSets
        println(selectedSets)
        return selectedSets.map { setId ->
            TcgDexSetSummaryDto(
                id = setId,
                name = ""
            )
        }
    }
}