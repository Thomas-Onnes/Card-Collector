package external.scryfall

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import external.scryfall.dto.ScryfallCardDto
import external.scryfall.dto.ScryfallListResponse
import external.scryfall.dto.ScryfallSetDto
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ScryfallClient {

    private val baseUrl = "https://api.scryfall.com"
    private val client = HttpClient.newBuilder().build()
    private val objectMapper = jacksonObjectMapper()

    fun getSets(): List<ScryfallSetDto> {
        val endpoint = "$baseUrl/sets"

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Accept", "application/json")
            .header("User-Agent", "Card-Collector/1.0")
            .GET()
            .build()

        val response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw IllegalStateException(
                "Scryfall API error: ${response.statusCode()}"
            )
        }

        val listResponse =
            objectMapper.readValue<ScryfallListResponse<ScryfallSetDto>>(
                response.body()
            )

        return listResponse.data
    }

    fun getCardsBySet(setCode: String): List<ScryfallCardDto> {
        val cards = mutableListOf<ScryfallCardDto>()

        var endpoint: String? =
            "$baseUrl/cards/search?q=set:$setCode"

        while (endpoint != null) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .header("User-Agent", "Card-Collector/1.0")
                .GET()
                .build()

            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            if (response.statusCode() != 200) {
                throw IllegalStateException(
                    "Scryfall API error: ${response.statusCode()}"
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
}