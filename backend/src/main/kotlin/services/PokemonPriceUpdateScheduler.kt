package services

import database.Database
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PokemonPriceUpdateScheduler(
    private val pokemonSets: List<String>
) {

    private val executor =
        Executors.newSingleThreadScheduledExecutor()

    fun start(
        initialDelayMinutes: Long,
        intervalMinutes: Long
    ) {
        if (pokemonSets.isEmpty()) {
            println("Pokemon price update scheduler disabled because no Pokemon sets are configured")
            return
        }

        println(
            "Pokemon price update scheduler enabled. " +
                    "Initial delay: $initialDelayMinutes minutes, interval: $intervalMinutes minutes"
        )

        executor.scheduleWithFixedDelay(
            {
                for (setId in pokemonSets) {
                    updatePokemonSet(setId)
                }
            },
            initialDelayMinutes,
            intervalMinutes,
            TimeUnit.MINUTES
        )
    }

    fun stop() {
        executor.shutdownNow()
    }

    private fun updatePokemonSet(setId: String) {
        val cleanSetId = setId.trim().lowercase()

        if (cleanSetId.isBlank()) {
            return
        }

        try {
            println("Starting scheduled Pokemon set update: $cleanSetId")

            Database.connect().use { connection ->
                val imported = CardImportService(connection).importPokemonSet(cleanSetId)
                println("Scheduled Pokemon set update finished: $cleanSetId ($imported cards)")
            }
        } catch (e: Exception) {
            println("Scheduled Pokemon set update failed for $cleanSetId: ${e::class.java.simpleName}: ${e.message}")
        }
    }
}