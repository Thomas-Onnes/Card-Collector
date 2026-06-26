package com.example.cardcollector.api

import com.example.cardcollector.models.CardSearchResult
import com.example.cardcollector.models.CollectionCardItem
import com.example.cardcollector.models.CollectionCardsResponse
import com.example.cardcollector.models.CollectionItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiClient {

    fun register(
        username: String,
        email: String,
        password: String
    ) {
        val body = JSONObject()
            .put("username", username)
            .put("email", email)
            .put("password", password)

        request(
            method = "POST",
            path = "/register",
            body = body
        )
    }

    fun login(
        email: String,
        password: String
    ): JSONObject {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)

        return request(
            method = "POST",
            path = "/login",
            body = body
        )
    }

    fun logout(token: String) {
        request(
            method = "POST",
            path = "/logout",
            token = token
        )
    }

    fun getCollections(token: String): List<CollectionItem> {
        val response = request(
            method = "GET",
            path = "/collections",
            token = token
        )

        val collectionsJson = if (response.has("collections")) {
            response.getJSONArray("collections")
        } else {
            response.getJSONArray("data")
        }

        return parseCollections(collectionsJson)
    }

    fun createCollection(
        token: String,
        collectionName: String,
        gameType: String
    ): CollectionItem {
        val body = JSONObject()
            .put("collectionName", collectionName)
            .put("gameType", gameType)

        val response = request(
            method = "POST",
            path = "/collections",
            token = token,
            body = body
        )

        return CollectionItem(
            collectionId = response.getInt("collectionId"),
            collectionName = response.getString("collectionName"),
            gameType = response.getString("gameType")
        )
    }

    fun deleteCollection(
        token: String,
        collectionId: Int
    ) {
        request(
            method = "DELETE",
            path = "/collections/$collectionId",
            token = token
        )
    }

    fun getCollectionCards(
        token: String,
        collectionId: Int
    ): CollectionCardsResponse {
        val response = request(
            method = "GET",
            path = "/collections/$collectionId/cards",
            token = token
        )

        return CollectionCardsResponse(
            cards = parseCollectionCards(response.getJSONArray("cards")),
            totalPrice = response.optDouble("totalPrice", 0.0)
        )
    }

    fun addCardToCollection(
        token: String,
        collectionId: Int,
        cardId: Int,
        quantity: Int,
        cardCondition: String,
        isFoil: Boolean,
        language: String
    ): CollectionCardsResponse {
        val body = JSONObject()
            .put("cardId", cardId)
            .put("quantity", quantity)
            .put("cardCondition", cardCondition)
            .put("isFoil", isFoil)
            .put("language", language)

        val response = request(
            method = "POST",
            path = "/collections/$collectionId/cards",
            token = token,
            body = body
        )

        return CollectionCardsResponse(
            cards = parseCollectionCards(response.getJSONArray("cards")),
            totalPrice = response.optDouble("totalPrice", 0.0)
        )
    }

    fun removeCardFromCollection(
        token: String,
        collectionId: Int,
        collectionCardId: Int,
        quantity: Int
    ): CollectionCardsResponse {
        val body = JSONObject()
            .put("quantity", quantity)

        val response = request(
            method = "POST",
            path = "/collections/$collectionId/cards/$collectionCardId/remove",
            token = token,
            body = body
        )

        return CollectionCardsResponse(
            cards = parseCollectionCards(response.getJSONArray("cards")),
            totalPrice = response.optDouble("totalPrice", 0.0)
        )
    }

    fun searchCards(
        token: String,
        gameType: String,
        name: String = "",
        setQuery: String = "",
        collectorNumber: String = "",
        rarity: String = "",
        artistOrIllustrator: String = ""
    ): List<CardSearchResult> {
        val path = "/cards/search" +
                "?gameType=${encode(gameType)}" +
                "&name=${encode(name)}" +
                "&set=${encode(setQuery)}" +
                "&collectorNumber=${encode(collectorNumber)}" +
                "&rarity=${encode(rarity)}" +
                "&artistOrIllustrator=${encode(artistOrIllustrator)}"

        val response = request(
            method = "GET",
            path = path,
            token = token
        )

        val array = if (response.has("data")) {
            response.getJSONArray("data")
        } else {
            JSONArray()
        }

        return parseCardSearchResults(array)
    }

    private fun request(
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null
    ): JSONObject {
        val url = URL(ApiConfig.BASE_URL + path)

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")

            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null) {
                doOutput = true
            }
        }

        try {
            if (body != null) {
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                }
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection, statusCode)

            if (statusCode !in 200..299) {
                val errorMessage = extractErrorMessage(responseText)
                throw ApiException(statusCode, errorMessage)
            }

            if (responseText.isBlank()) {
                return JSONObject()
            }

            return if (responseText.trim().startsWith("[")) {
                JSONObject().put("data", JSONArray(responseText))
            } else {
                JSONObject(responseText)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(
        connection: HttpURLConnection,
        statusCode: Int
    ): String {
        val stream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return BufferedReader(stream.reader(Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }

    private fun extractErrorMessage(responseText: String): String {
        return try {
            val json = JSONObject(responseText)
            json.optString("error", "Request failed")
        } catch (e: Exception) {
            "Request failed"
        }
    }

    private fun parseCollections(jsonArray: JSONArray): List<CollectionItem> {
        val collections = mutableListOf<CollectionItem>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(index)

            collections.add(
                CollectionItem(
                    collectionId = item.getInt("collectionId"),
                    collectionName = item.getString("collectionName"),
                    gameType = item.getString("gameType")
                )
            )
        }

        return collections
    }

    private fun parseCardSearchResults(jsonArray: JSONArray): List<CardSearchResult> {
        val cards = mutableListOf<CardSearchResult>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(index)

            cards.add(
                CardSearchResult(
                    cardId = item.getInt("cardId"),
                    gameType = item.getString("gameType"),
                    name = item.getString("name"),
                    imageUrl = item.optNullableString("imageUrl"),
                    setName = item.optNullableString("setName"),
                    setCode = item.optNullableString("setCode"),
                    collectorNumber = item.optNullableString("collectorNumber"),
                    rarity = item.optNullableString("rarity"),
                    artistOrIllustrator = item.optNullableString("artistOrIllustrator"),
                    price = item.optNullableDouble("price")
                )
            )
        }

        return cards
    }

    private fun parseCollectionCards(jsonArray: JSONArray): List<CollectionCardItem> {
        val cards = mutableListOf<CollectionCardItem>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(index)

            cards.add(
                CollectionCardItem(
                    collectionCardId = item.getInt("collectionCardId"),
                    cardId = item.getInt("cardId"),
                    name = item.getString("name"),
                    gameType = item.getString("gameType"),
                    imageUrl = item.optNullableString("imageUrl"),
                    quantity = item.getInt("quantity"),
                    cardCondition = item.getString("cardCondition"),
                    isFoil = item.getBoolean("isFoil"),
                    language = item.getString("language"),
                    setName = item.optNullableString("setName"),
                    setCode = item.optNullableString("setCode"),
                    collectorNumber = item.optNullableString("collectorNumber"),
                    rarity = item.optNullableString("rarity"),
                    artistOrIllustrator = item.optNullableString("artistOrIllustrator"),
                    price = item.optNullableDouble("price")
                )
            )
        }

        return cards
    }

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) {
            return null
        }

        return optString(key)
    }

    private fun JSONObject.optNullableDouble(key: String): Double? {
        if (!has(key) || isNull(key)) {
            return null
        }

        return optDouble(key)
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }
}