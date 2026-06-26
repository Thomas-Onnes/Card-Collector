package api

import com.sun.net.httpserver.HttpExchange
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

object HttpUtils {

    const val MAX_REQUEST_BODY_BYTES = 8 * 1024

    fun readRequestBody(exchange: HttpExchange): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var totalBytes = 0

        exchange.requestBody.use { input ->
            while (true) {
                val read = input.read(buffer)

                if (read == -1) {
                    break
                }

                totalBytes += read

                if (totalBytes > MAX_REQUEST_BODY_BYTES) {
                    throw IllegalArgumentException("Request body too large")
                }

                output.write(buffer, 0, read)
            }
        }

        return output.toString(StandardCharsets.UTF_8)
    }

    fun sendJson(exchange: HttpExchange, statusCode: Int, json: String) {
        val responseBytes = json.toByteArray(StandardCharsets.UTF_8)

        exchange.responseHeaders.add(
            "Content-Type",
            "application/json; charset=utf-8"
        )

        exchange.sendResponseHeaders(
            statusCode,
            responseBytes.size.toLong()
        )

        exchange.responseBody.use { body ->
            body.write(responseBytes)
        }
    }

    fun sendEmpty(exchange: HttpExchange, statusCode: Int) {
        exchange.sendResponseHeaders(statusCode, -1)
        exchange.close()
    }
}