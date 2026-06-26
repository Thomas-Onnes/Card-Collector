package api

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import database.Database
import external.scryfall.ScryfallClient
import repositories.CardRepository
import repositories.MagicTheGatheringCardRepository
import security.TokenService
import services.BulkCardImportService
import services.CardImportService
import services.MagicTheGatheringPriceUpdateService
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class CardsHandler : HttpHandler {

    private val gson = Gson()
    private val cardRepository = CardRepository()

    override fun handle(exchange: HttpExchange) {
        try {
            val session = getSession(exchange)

            if (session == null) {
                HttpUtils.sendJson(
                    exchange,
                    401,
                    """{"error":"Unauthorized"}"""
                )
                return
            }

            val path = exchange.requestURI.path
            val method = exchange.requestMethod

            when {
                path == "/cards/search" && method == "GET" -> {
                    handleSearchCards(exchange)
                }

                path == "/cards/import/pokemon" && method == "POST" -> {
                    if (!requireManualImportAccess(exchange)) {
                        return
                    }

                    handleImportPokemonSet(exchange)
                }

                path == "/cards/import/mtg" && method == "POST" -> {
                    if (!requireManualImportAccess(exchange)) {
                        return
                    }

                    handleImportMagicSet(exchange)
                }

                path == "/cards/update-prices/mtg" && method == "POST" -> {
                    if (!requireManualImportAccess(exchange)) {
                        return
                    }

                    handleUpdateMagicPrices(exchange)
                }

                path == "/cards/import/bulk" && method == "POST" -> {
                    if (!requireManualImportAccess(exchange)) {
                        return
                    }

                    handleBulkImport(exchange)
                }

                path == "/cards/update-prices/all" && method == "POST" -> {
                    if (!requireManualImportAccess(exchange)) {
                        return
                    }

                    handleUpdateAllPrices(exchange)
                }

                else -> {
                    HttpUtils.sendEmpty(exchange, 404)
                }
            }
        } catch (e: IllegalArgumentException) {
            HttpUtils.sendJson(
                exchange,
                400,
                gson.toJson(mapOf("error" to (e.message ?: "Invalid card request")))
            )
        } catch (e: Exception) {
            println("Cards error: ${e::class.java.simpleName}: ${e.message}")

            HttpUtils.sendJson(
                exchange,
                500,
                """{"error":"Card request failed"}"""
            )
        }
    }

    private fun handleSearchCards(exchange: HttpExchange) {
        val queryParams = parseQuery(exchange.requestURI.rawQuery)

        val gameType = queryParams["gameType"]?.trim()?.lowercase()
            ?: throw IllegalArgumentException("Game type is required")

        val results = cardRepository.searchCards(
            gameType = gameType,
            name = queryParams["name"].orEmpty(),
            setQuery = queryParams["set"].orEmpty(),
            collectorNumber = queryParams["collectorNumber"].orEmpty(),
            rarity = queryParams["rarity"].orEmpty(),
            artistOrIllustrator = queryParams["artistOrIllustrator"].orEmpty()
        )

        HttpUtils.sendJson(
            exchange,
            200,
            gson.toJson(results)
        )
    }

    private fun handleImportPokemonSet(exchange: HttpExchange) {
        val queryParams = parseQuery(exchange.requestURI.rawQuery)
        val setId = queryParams["set"]?.trim()
            ?: throw IllegalArgumentException("Pokemon set is required")

        Database.connect().use { connection ->
            val imported = CardImportService(connection).importPokemonSet(setId)

            HttpUtils.sendJson(
                exchange,
                200,
                gson.toJson(
                    mapOf(
                        "message" to "Pokemon set imported",
                        "set" to setId,
                        "importedCards" to imported
                    )
                )
            )
        }
    }

    private fun handleImportMagicSet(exchange: HttpExchange) {
        val queryParams = parseQuery(exchange.requestURI.rawQuery)
        val setCode = queryParams["set"]?.trim()
            ?: throw IllegalArgumentException("Magic set is required")

        Database.connect().use { connection ->
            val imported = CardImportService(connection).importMagicSet(setCode)

            HttpUtils.sendJson(
                exchange,
                200,
                gson.toJson(
                    mapOf(
                        "message" to "Magic set imported",
                        "set" to setCode,
                        "importedCards" to imported
                    )
                )
            )
        }
    }

    private fun handleUpdateMagicPrices(exchange: HttpExchange) {
        Database.connect().use { connection ->
            val service = MagicTheGatheringPriceUpdateService(
                scryfallClient = ScryfallClient(),
                magicCardRepository = MagicTheGatheringCardRepository(connection)
            )

            service.updateAllPrices()

            HttpUtils.sendJson(
                exchange,
                200,
                gson.toJson(mapOf("message" to "Magic card prices updated"))
            )
        }
    }

    private fun handleBulkImport(exchange: HttpExchange) {
        val queryParams = parseQuery(exchange.requestURI.rawQuery)

        val importPokemon = queryParams["pokemon"]?.toBooleanStrictOrNull() ?: true
        val importMagic = queryParams["mtg"]?.toBooleanStrictOrNull() ?: true
        val force = queryParams["force"]?.toBooleanStrictOrNull() ?: true
        val pokemonMaxCards = queryParams["pokemonMax"]?.toIntOrNull() ?: 0
        val magicMaxCards = queryParams["mtgMax"]?.toIntOrNull() ?: 0
        val requestDelayMillis = queryParams["delayMs"]?.toLongOrNull() ?: 0L

        Database.connect().use { connection ->
            val result = BulkCardImportService(connection).runStartupBulkImport(
                importPokemon = importPokemon,
                importMagic = importMagic,
                forceImport = force,
                pokemonMaxCards = pokemonMaxCards,
                magicMaxCards = magicMaxCards,
                requestDelayMillis = requestDelayMillis
            )

            HttpUtils.sendJson(
                exchange,
                200,
                gson.toJson(
                    mapOf(
                        "message" to "Bulk import finished",
                        "importedPokemonCards" to result.importedPokemonCards,
                        "importedMagicCards" to result.importedMagicCards
                    )
                )
            )
        }
    }

    private fun handleUpdateAllPrices(exchange: HttpExchange) {
        val queryParams = parseQuery(exchange.requestURI.rawQuery)

        val updatePokemon = queryParams["pokemon"]?.toBooleanStrictOrNull() ?: true
        val updateMagic = queryParams["mtg"]?.toBooleanStrictOrNull() ?: true
        val pokemonMaxCards = queryParams["pokemonMax"]?.toIntOrNull() ?: 0
        val magicMaxCards = queryParams["mtgMax"]?.toIntOrNull() ?: 0
        val requestDelayMillis = queryParams["delayMs"]?.toLongOrNull() ?: 0L

        Database.connect().use { connection ->
            val result = BulkCardImportService(connection).runPriceUpdateImport(
                updatePokemon = updatePokemon,
                updateMagic = updateMagic,
                pokemonMaxCards = pokemonMaxCards,
                magicMaxCards = magicMaxCards,
                requestDelayMillis = requestDelayMillis
            )

            HttpUtils.sendJson(
                exchange,
                200,
                gson.toJson(
                    mapOf(
                        "message" to "Bulk price update finished",
                        "updatedPokemonCards" to result.importedPokemonCards,
                        "updatedMagicCards" to result.importedMagicCards
                    )
                )
            )
        }
    }

    private fun requireManualImportAccess(exchange: HttpExchange): Boolean {
        val manualImportEnabled = envFlag(
            name = "MANUAL_CARD_IMPORT_ENABLED",
            defaultValue = false
        )

        if (!manualImportEnabled) {
            HttpUtils.sendJson(
                exchange,
                403,
                """{"error":"Manual card import is disabled"}"""
            )
            return false
        }

        val expectedToken = System.getenv("ADMIN_IMPORT_TOKEN")
            ?.trim()

        if (expectedToken.isNullOrBlank()) {
            println("Manual card import blocked because ADMIN_IMPORT_TOKEN is not configured")

            HttpUtils.sendJson(
                exchange,
                403,
                """{"error":"Manual card import is not configured"}"""
            )
            return false
        }

        val providedToken = exchange.requestHeaders
            .getFirst("X-Admin-Import-Token")
            ?.trim()

        if (!secureEquals(providedToken, expectedToken)) {
            HttpUtils.sendJson(
                exchange,
                403,
                """{"error":"Forbidden"}"""
            )
            return false
        }

        return true
    }

    private fun secureEquals(
        providedValue: String?,
        expectedValue: String
    ): Boolean {
        if (providedValue.isNullOrBlank()) {
            return false
        }

        val providedBytes = providedValue.toByteArray(StandardCharsets.UTF_8)
        val expectedBytes = expectedValue.toByteArray(StandardCharsets.UTF_8)

        return MessageDigest.isEqual(
            providedBytes,
            expectedBytes
        )
    }

    private fun envFlag(
        name: String,
        defaultValue: Boolean
    ): Boolean {
        return when (System.getenv(name)?.trim()?.lowercase()) {
            "true", "1", "yes", "y", "on" -> true
            "false", "0", "no", "n", "off" -> false
            null, "" -> defaultValue
            else -> defaultValue
        }
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) {
            return emptyMap()
        }

        return rawQuery
            .split("&")
            .mapNotNull { part ->
                val pieces = part.split("=", limit = 2)

                if (pieces.isEmpty() || pieces[0].isBlank()) {
                    null
                } else {
                    val key = decode(pieces[0])
                    val value = if (pieces.size > 1) decode(pieces[1]) else ""
                    key to value
                }
            }
            .toMap()
    }

    private fun decode(value: String): String {
        return URLDecoder.decode(value, StandardCharsets.UTF_8)
    }

    private fun getSession(exchange: HttpExchange): TokenService.UserSession? {
        val authHeader = exchange.requestHeaders.getFirst("Authorization")

        val token = authHeader
            ?.removePrefix("Bearer")
            ?.trim()

        return TokenService.validateToken(token)
    }
}