package external.scryfall

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import external.scryfall.dto.ScryfallCardDto
import external.scryfall.dto.ScryfallListResponse
import external.scryfall.dto.ScryfallSetDto
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper
import external.scryfall.dto.ScryfallCollectionRequestDto
import external.scryfall.dto.ScryfallIdentifierDto

class ScryfallClient {

    private val baseUrl = "https://api.scryfall.com"
    private val client = HttpClient.newBuilder().build()
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val allowedRarities = setOf(
        "common",
        "uncommon",
        "rare",
        "mythic",
        "special",
        "bonus"
    )

    fun getSets(): List<ScryfallSetDto> {
        val endpoint = "$baseUrl/sets"

        val request = createGetRequest(endpoint)

        val response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw IllegalStateException(
                "Scryfall API error while fetching sets: ${response.statusCode()} ${response.body()}"
            )
        }

        val listResponse =
            objectMapper.readValue<ScryfallListResponse<ScryfallSetDto>>(
                response.body()
            )

        val validSets = listResponse.data.filter { set ->
            val isValid = isValidScryfallSet(set)

            if (!isValid) {
                println("Skipping invalid Scryfall set")
            }

            isValid
        }

        return validSets
    }

    fun getCardsBySet(
        setCode: String,
        searchUri: String? = null
    ): List<ScryfallCardDto> {
        val cards = mutableListOf<ScryfallCardDto>()

        var endpoint: String? =
            searchUri ?: createCardsBySetEndpoint(setCode)

        while (endpoint != null) {
            val request = createGetRequest(endpoint)

            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() == 404) {
                println(
                    "No Scryfall cards found for set $setCode. Skipping this set."
                )
                return cards
            }

            if (response.statusCode() != 200) {
                throw IllegalStateException(
                    "Scryfall API error while fetching cards for set $setCode: ${response.statusCode()} ${response.body()}"
                )
            }

            val listResponse =
                objectMapper.readValue<ScryfallListResponse<ScryfallCardDto>>(
                    response.body()
                )

            val validCards = listResponse.data.filter { card ->
                val isValid = isValidScryfallCard(card)

                if (!isValid) {
                    println("Skipping invalid Scryfall card from set $setCode")
                }

                isValid
            }

            cards.addAll(validCards)

            endpoint = listResponse.nextPage

            Thread.sleep(600)

        }

        return cards
    }

    private fun createCardsBySetEndpoint(setCode: String): String {
        val query = URLEncoder.encode(
            "set:$setCode",
            StandardCharsets.UTF_8
        )

        return "$baseUrl/cards/search?q=$query&unique=prints"
    }

    private fun createGetRequest(endpoint: String): HttpRequest {
        return HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Accept", "application/json")
            .header("User-Agent", "Card-Collector/1.0")
            .GET()
            .build()
    }

    private fun isValidScryfallCard(card: ScryfallCardDto): Boolean {
        if (!isValidUuid(card.id)) {
            return false
        }

        if (!isValidRequiredText(card.name, 100)) {
            return false
        }

        if (!isValidRequiredText(card.setCode, 10)) {
            return false
        }

        if (!isValidRequiredText(card.setName, 200)) {
            return false
        }

        if (card.rarity.lowercase() !in allowedRarities) {
            return false
        }

        if (!isValidOptionalText(card.manaCost, 100)) {
            return false
        }

        if (!isValidOptionalText(card.typeLine, 100)) {
            return false
        }

        if (!isValidOptionalText(card.illustrator, 200)) {
            return false
        }

        return true
    }

    private fun isValidScryfallSet(set: ScryfallSetDto): Boolean {
        if (!isValidUuid(set.id)) {
            return false
        }

        if (!isValidRequiredText(set.code, 10)) {
            return false
        }

        if (!isValidRequiredText(set.name, 200)) {
            return false
        }

        return true
    }

    private fun isValidUuid(value: String): Boolean {
        return try {
            UUID.fromString(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun isValidRequiredText(
        value: String,
        maxLength: Int
    ): Boolean {
        return value.isNotBlank() &&
                value.length <= maxLength &&
                !containsUnsafeControlCharacters(value)
    }

    private fun isValidOptionalText(
        value: String?,
        maxLength: Int
    ): Boolean {
        if (value == null) {
            return true
        }

        return value.length <= maxLength &&
                !containsUnsafeControlCharacters(value)
    }

    private fun containsUnsafeControlCharacters(value: String): Boolean {
        return value.any { character ->
            character.code < 32 &&
                    character != '\n' &&
                    character != '\r' &&
                    character != '\t'
        }
    }

    fun getCardsByIds(scryfallIds: List<String>): List<ScryfallCardDto> {
        val cards = arrayListOf<ScryfallCardDto>()

        val chunks = scryfallIds.chunked(75)

        for (chunk in chunks) {
            val endpoint = "$baseUrl/cards/collection"

            val requestBody = ScryfallCollectionRequestDto(
                identifiers = chunk.map { id ->
                    ScryfallIdentifierDto(id = id)
                }
            )

            val jsonBody = objectMapper.writeValueAsString(requestBody)

            val request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Card-Collector/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() != 200) {
                throw IllegalStateException(
                    "Scryfall API error while updating prices: ${response.statusCode()} ${response.body()}"
                )
            }

            val listResponse =
                objectMapper.readValue<ScryfallListResponse<ScryfallCardDto>>(
                    response.body()
                )

            cards.addAll(listResponse.data)

            if (chunk != chunks.last()) {
                Thread.sleep(600)
            }
        }

        return cards
    }
}
