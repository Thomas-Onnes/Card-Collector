package com.example.cardcollector.api

import com.example.cardcollector.models.CollectionItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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
}
