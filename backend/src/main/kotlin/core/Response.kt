package core

data class Response(
    val statusCode: Int,
    val body: String,
    val contentType: String = "application/json"
)