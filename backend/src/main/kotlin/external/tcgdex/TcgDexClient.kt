package external.tcgdex

import exceptions.CardNotFoundException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TcgDexClient {

    private val baseUrl = "https://api.tcgdex.net/v2/"
    private val client = HttpClient.newBuilder().build()

    fun getCard(cardId: String,languageCode: String = "en"): String {
        val endpoint = "$baseUrl/$languageCode/cards/$cardId"

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

       return when (response.statusCode()) {

           200 -> { response.body() }

           404 -> { throw CardNotFoundException("Card is not found") }

           else -> { throw IllegalStateException("The API has an issue") }
       }
    }
}