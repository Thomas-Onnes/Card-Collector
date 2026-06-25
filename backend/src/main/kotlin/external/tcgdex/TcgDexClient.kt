package external.tcgdex

import exceptions.CardNotFoundException
import external.tcgdex.dto.TcgDexCardDto
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import exceptions.SetNotFoundException
import external.tcgdex.dto.TcgDexCardResponse
import external.tcgdex.dto.TcgDexSetDto
import external.tcgdex.dto.TcgDexSetResponse
import config.Environment.importAllPokemonSets

class TcgDexClient {

    private val baseUrl = "https://api.tcgdex.net/v2/"
    private val client = HttpClient.newBuilder().build()
    private val objectMapper = jacksonObjectMapper()

    fun getCard(cardId: String,languageCode: String = "en"): TcgDexCardResponse {
        val endpoint = "$baseUrl/$languageCode/cards/$cardId"

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .GET()
            .build()

        val response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        )

        return when (response.statusCode()) {

            200 -> {
                val responseBody = response.body()
                val dto = objectMapper.readValue<TcgDexCardDto>(
                    responseBody
                )

                TcgDexCardResponse(
                    dto = dto,
                    rawJson = responseBody
                )
            }

           404 -> { throw CardNotFoundException("Card is not found") }

           else -> { throw IllegalStateException("The API has an issue") }
        }
    }

    fun getSet(setId: String,languageCode: String = "en"): TcgDexSetResponse {
        val endpoint = "$baseUrl/$languageCode/sets/$setId"

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .GET()
            .build()

        val response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        )

        return when (response.statusCode()) {

            200 -> {
                val responseBody = response.body()
                val dto = objectMapper.readValue<TcgDexSetDto>(
                    responseBody
                )

                TcgDexSetResponse(
                    dto = dto,
                    rawJson = responseBody
                )
            }

            404 -> { throw SetNotFoundException("Card is not found")
            }

            else -> { throw IllegalStateException("The API has an issue") }
        }
    }
//
//    fun getAllSets(languageCode: String = "en"):List<TcgDexSetResponse> {
//        val endpoint = "$baseUrl/$languageCode/sets"
//
//        val request = HttpRequest.newBuilder()
//            .uri(URI.create(endpoint))
//            .GET()
//            .build()
//
//        val response = client.send(
//            request, HttpResponse.BodyHandlers.ofString()
//        )
//
//        return when (response.statusCode()) {
//
//            200 -> {
//                val responseBody = response.body()
//                val dto = objectMapper.readValue<List<TcgDexSetDto>>(
//                    responseBody
//                )
//
//                TcgDexSetResponse(
//                    dto = dto,
//                    rawJson = responseBody
//                )
//            }
//
//            404 -> { throw SetNotFoundException("Card is not found")
//            }
//
//            else -> { throw IllegalStateException("The API has an issue") }
//        }
//    }
}