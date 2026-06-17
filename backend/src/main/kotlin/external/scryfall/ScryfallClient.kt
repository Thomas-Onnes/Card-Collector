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

class ScryfallClient {

    private val baseUrl = "https://api.scryfall.com"
    private val client = HttpClient.newBuilder().build()
    private val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

        return listResponse.data
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

            cards.addAll(listResponse.data)

            endpoint = listResponse.nextPage

            if (endpoint != null) {
                Thread.sleep(600)
            }
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
}
