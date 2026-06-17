package config

import java.io.File

object PathConfig {

    val resourcePath: String = listOf(
        "src/main/resources",
        "backend/src/main/resources"
    ).firstOrNull { path ->
        File(path).exists()
    } ?: "src/main/resources"
}
