package services

import database.Database
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BulkCardImportScheduler(
    private val updatePokemon: Boolean,
    private val updateMagic: Boolean,
    private val pokemonMaxCards: Int,
    private val magicMaxCards: Int,
    private val requestDelayMillis: Long
) {

    private val executor = Executors.newSingleThreadScheduledExecutor()

    fun start(
        initialDelayMinutes: Long,
        intervalMinutes: Long
    ) {
        if (!updatePokemon && !updateMagic) {
            println("Bulk price update scheduler disabled because both games are disabled")
            return
        }

        println(
            "Bulk price update scheduler enabled. " +
                "Initial delay: $initialDelayMinutes minutes, interval: $intervalMinutes minutes"
        )

        executor.scheduleWithFixedDelay(
            {
                try {
                    println("Starting scheduled bulk price update")

                    Database.connect().use { connection ->
                        BulkCardImportService(connection).runPriceUpdateImport(
                            updatePokemon = updatePokemon,
                            updateMagic = updateMagic,
                            pokemonMaxCards = pokemonMaxCards,
                            magicMaxCards = magicMaxCards,
                            requestDelayMillis = requestDelayMillis
                        )
                    }

                    println("Scheduled bulk price update finished")
                } catch (e: Exception) {
                    println("Scheduled bulk price update failed: ${e::class.java.simpleName}: ${e.message}")
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
}
