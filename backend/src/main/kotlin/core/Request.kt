package core

data class Request (
    val method: String,
    val path: String,
    val body: String? = null
)