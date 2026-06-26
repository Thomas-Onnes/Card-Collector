package services

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MagicTheGatheringPriceUpdateScheduler(
    private val priceUpdateService: MagicTheGatheringPriceUpdateService
) {
    private val executor =
        Executors.newSingleThreadScheduledExecutor()

    fun start() {
        executor.scheduleWithFixedDelay(
            {
                try {
                    println("Updating Magic card prices")
                    priceUpdateService.updateAllPrices()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            0,
            10,
            TimeUnit.MINUTES
        )
    }
}