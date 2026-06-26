package services

import external.scryfall.ScryfallClient
import external.scryfall.dto.ScryfallCardDto
import external.scryfall.mapper.toMagicTheGatheringCard
import external.tcgdex.TcgDexClient
import external.tcgdex.dto.TcgDexCardDto
import external.tcgdex.mapper.TcgDexCardMapper
import models.Card
import models.MagicTheGatheringSet
import models.PokemonSet
import repositories.ApiSyncStateRepository
import repositories.CardRepository
import repositories.MagicTheGatheringCardRepository
import repositories.MagicTheGatheringSetRepository
import repositories.PokemonCardRepository
import repositories.PokemonSetRepository
import java.sql.Connection
import java.sql.Date

class BulkCardImportService(
    private val connection: Connection,
    private val scryfallClient: ScryfallClient = ScryfallClient(),
    private val tcgDexClient: TcgDexClient = TcgDexClient()
) {

    data class BulkImportResult(
        val importedPokemonCards: Int,
        val importedMagicCards: Int
    )

    private val cardRepository = CardRepository()
    private val magicSetRepository = MagicTheGatheringSetRepository(connection)
    private val magicCardRepository = MagicTheGatheringCardRepository(connection)
    private val pokemonSetRepository = PokemonSetRepository(connection)
    private val pokemonCardRepository = PokemonCardRepository(connection)
    private val apiSyncStateRepository = ApiSyncStateRepository(connection)
    private val pokemonCardMapper = TcgDexCardMapper()

    fun runStartupBulkImport(
        importPokemon: Boolean,
        importMagic: Boolean,
        forceImport: Boolean,
        pokemonMaxCards: Int,
        magicMaxCards: Int,
        requestDelayMillis: Long
    ): BulkImportResult {
        var importedPokemonCards = 0
        var importedMagicCards = 0

        if (importPokemon) {
            importedPokemonCards = runImportOnce(
                syncName = "pokemon_bulk_import",
                forceImport = forceImport
            ) {
                importAllPokemonCards(
                    maxCards = pokemonMaxCards,
                    requestDelayMillis = requestDelayMillis
                )
            }
        } else {
            println("Pokemon bulk import disabled")
        }

        if (importMagic) {
            importedMagicCards = runImportOnce(
                syncName = "mtg_bulk_import",
                forceImport = forceImport
            ) {
                importAllMagicCards(
                    maxCards = magicMaxCards
                )
            }
        } else {
            println("Magic bulk import disabled")
        }

        return BulkImportResult(
            importedPokemonCards = importedPokemonCards,
            importedMagicCards = importedMagicCards
        )
    }

    fun runPriceUpdateImport(
        updatePokemon: Boolean,
        updateMagic: Boolean,
        pokemonMaxCards: Int,
        magicMaxCards: Int,
        requestDelayMillis: Long
    ): BulkImportResult {
        val updatedPokemonCards = if (updatePokemon) {
            updatePokemonPriceBatch(
                maxCards = pokemonMaxCards,
                requestDelayMillis = requestDelayMillis
            )
        } else {
            println("Pokemon price update disabled")
            0
        }

        val updatedMagicCards = if (updateMagic) {
            importAllMagicCards(
                maxCards = magicMaxCards
            )
        } else {
            println("Magic price update disabled")
            0
        }

        return BulkImportResult(
            importedPokemonCards = updatedPokemonCards,
            importedMagicCards = updatedMagicCards
        )
    }

    private fun updatePokemonPriceBatch(
        maxCards: Int,
        requestDelayMillis: Long
    ): Int {
        val batchSize = if (maxCards > 0) maxCards else 50

        println("Starting Pokemon price update batch. Batch size: $batchSize")

        return PokemonPriceUpdateService(
            connection = connection,
            cardRepository = cardRepository,
            pokemonCardRepository = pokemonCardRepository,
            tcgDexClient = tcgDexClient
        ).updateNextBatch(
            limit = batchSize,
            requestDelayMillis = requestDelayMillis
        )
    }

    private fun runImportOnce(
        syncName: String,
        forceImport: Boolean,
        importAction: () -> Int
    ): Int {
        if (!forceImport && apiSyncStateRepository.isCompleted(syncName)) {
            println("$syncName already completed. Skipping startup import.")
            return 0
        }

        val imported = importAction()
        apiSyncStateRepository.markCompleted(syncName)
        return imported
    }

    private fun importAllMagicCards(maxCards: Int): Int {
        println("Starting Magic bulk import from Scryfall default-cards bulk data")

        val imported = scryfallClient.streamDefaultCardsBulkData(
            maxCards = maxCards
        ) { cardDto ->
            saveMagicCard(cardDto)
        }

        println("Magic bulk import finished. Imported/updated $imported cards")
        return imported
    }

    private fun saveMagicCard(cardDto: ScryfallCardDto) {
        val set = MagicTheGatheringSet(
            scryfallId = cardDto.setId ?: "set-${cardDto.setCode.lowercase()}",
            code = cardDto.setCode.lowercase(),
            name = cardDto.setName
        )

        magicSetRepository.save(set)

        val magicCard = cardDto.toMagicTheGatheringCard()

        val baseCard = Card(
            id = null,
            gameType = "mtg",
            externalApiId = magicCard.scryfallId,
            name = magicCard.name,
            imageUrl = null
        )

        val generatedCardId = cardRepository.save(connection, baseCard)
        magicCardRepository.save(magicCard, generatedCardId)
    }

    private fun importAllPokemonCards(
        maxCards: Int,
        requestDelayMillis: Long
    ): Int {
        println("Starting Pokemon bulk import from TCGdex card list")

        val cardSummaries = tcgDexClient.getAllCards()
        val limitedCards = if (maxCards > 0) {
            cardSummaries.take(maxCards)
        } else {
            cardSummaries
        }

        val setIdCache = mutableMapOf<String, Int>()
        var imported = 0
        var skipped = 0

        for (summary in limitedCards) {
            try {
                val cardResponse = tcgDexClient.getCard(summary.id)
                val cardDto = cardResponse.dto
                val setId = resolvePokemonSetId(
                    cardDto = cardDto,
                    setIdCache = setIdCache
                )

                val baseCard = pokemonCardMapper.toCard(cardDto)
                val generatedCardId = cardRepository.save(connection, baseCard)

                val pokemonCard = pokemonCardMapper.toPokemonCard(
                    dto = cardDto,
                    rawJson = cardResponse.rawJson,
                    generatedCardId = generatedCardId,
                    generatedSetId = setId
                )

                pokemonCardRepository.save(pokemonCard)
                imported++

                if (imported % 500 == 0) {
                    println("Imported/updated $imported Pokemon cards from TCGdex")
                }

                if (requestDelayMillis > 0) {
                    Thread.sleep(requestDelayMillis)
                }
            } catch (e: Exception) {
                skipped++
                println("Skipping Pokemon card ${summary.id}: ${e::class.java.simpleName}: ${e.message}")
            }
        }

        println("Pokemon bulk import finished. Imported/updated $imported cards. Skipped $skipped cards.")
        return imported
    }

    private fun resolvePokemonSetId(
        cardDto: TcgDexCardDto,
        setIdCache: MutableMap<String, Int>
    ): Int {
        val tcgDexSetId = cardDto.set?.id
            ?: parseSetIdFromCardId(cardDto.id)
            ?: "unknown"

        val cachedSetId = setIdCache[tcgDexSetId]
        if (cachedSetId != null) {
            return cachedSetId
        }

        val savedSetId = try {
            val setResponse = tcgDexClient.getSet(tcgDexSetId)
            val setDto = setResponse.dto

            pokemonSetRepository.save(
                PokemonSet(
                    id = null,
                    tcgDexId = setDto.id,
                    name = setDto.name,
                    series = setDto.serie?.name ?: "Unknown",
                    releaseDate = parseDateOrDefault(setDto.releaseDate)
                )
            )
        } catch (e: Exception) {
            pokemonSetRepository.save(
                PokemonSet(
                    id = null,
                    tcgDexId = tcgDexSetId,
                    name = cardDto.set?.name ?: tcgDexSetId,
                    series = "Unknown",
                    releaseDate = Date.valueOf("1970-01-01")
                )
            )
        }

        setIdCache[tcgDexSetId] = savedSetId
        return savedSetId
    }

    private fun parseSetIdFromCardId(cardId: String): String? {
        val separatorIndex = cardId.lastIndexOf('-')

        if (separatorIndex <= 0) {
            return null
        }

        return cardId.substring(0, separatorIndex)
    }

    private fun parseDateOrDefault(value: String?): Date {
        return try {
            Date.valueOf(value ?: "1970-01-01")
        } catch (e: IllegalArgumentException) {
            Date.valueOf("1970-01-01")
        }
    }
}
