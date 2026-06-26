package pokemon

import pokemon.services.PokemonPriceUpdateService

class PokemonPriceUpdateScheduler(
    private val service: PokemonPriceUpdateService
) {
    fun start() {
        Thread {
            while (true) {

                try {
                    service.updatePrices()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Thread.sleep(60_000)
            }
        }.start()
    }
}