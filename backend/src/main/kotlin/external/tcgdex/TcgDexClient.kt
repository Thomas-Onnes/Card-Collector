package external.tcgdex

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import exceptions.CardNotFoundException
import exceptions.SetNotFoundException
import external.tcgdex.dto.TcgDexCardDto
import external.tcgdex.dto.TcgDexCardResponse
import external.tcgdex.dto.TcgDexCardSummaryDto
import external.tcgdex.dto.TcgDexSetDto
import external.tcgdex.dto.TcgDexSetResponse
import external.tcgdex.dto.TcgDexSetSummaryDto
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TcgDexClient {

    private val baseUrl = "https://api.tcgdex.net/v2"
    private val client = HttpClient.newBuilder().build()
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun getCard(
        cardId: String,
        languageCode: String = "en"
    ): TcgDexCardResponse {
        val endpoint = "$baseUrl/$languageCode/cards/$cardId"
        val response = getEndpoint(endpoint)

        return when (response.statusCode()) {
            200 -> {
                val responseBody = response.body()
                val dto = objectMapper.readValue<TcgDexCardDto>(responseBody)

                TcgDexCardResponse(
                    dto = dto,
                    rawJson = responseBody
                )
            }

            404 -> throw CardNotFoundException("Card was not found in TCGdex")
            else -> throw IllegalStateException("TCGdex API error: ${response.statusCode()}")
        }
    }

    fun getSet(
        setId: String,
        languageCode: String = "en"
    ): TcgDexSetResponse {
        val endpoint = "$baseUrl/$languageCode/sets/$setId"
        val response = getEndpoint(endpoint)

        return when (response.statusCode()) {
            200 -> {
                val responseBody = response.body()
                val dto = objectMapper.readValue<TcgDexSetDto>(responseBody)

                TcgDexSetResponse(
                    dto = dto,
                    rawJson = responseBody
                )
            }

            404 -> throw SetNotFoundException("Set was not found in TCGdex")
            else -> throw IllegalStateException("TCGdex API error: ${response.statusCode()}")
        }
    }

    fun getAllSets(languageCode: String = "en"): List<TcgDexSetSummaryDto> {
        val endpoint = "$baseUrl/$languageCode/sets"
        val response = getEndpoint(endpoint)

        return when (response.statusCode()) {
            200 -> objectMapper.readValue<List<TcgDexSetSummaryDto>>(response.body())
            404 -> throw SetNotFoundException("No TCGdex sets found")
            else -> throw IllegalStateException("TCGdex API error: ${response.statusCode()}")
        }
    }

    fun getAllCards(languageCode: String = "en"): List<TcgDexCardSummaryDto> {
        val endpoint = "$baseUrl/$languageCode/cards"
        val response = getEndpoint(endpoint)

        return when (response.statusCode()) {
            200 -> objectMapper.readValue<List<TcgDexCardSummaryDto>>(response.body())
            404 -> throw CardNotFoundException("No TCGdex cards found")
            else -> throw IllegalStateException("TCGdex API error: ${response.statusCode()}")
        }
    }

    private fun getEndpoint(endpoint: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Accept", "application/json")
            .header("User-Agent", "Card-Collector/1.0")
            .GET()
            .build()

        return client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )
    }
}