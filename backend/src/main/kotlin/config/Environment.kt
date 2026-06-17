package config

import io.github.cdimascio.dotenv.dotenv

object Environment {

    private val dotenv = dotenv {
        directory = "."
        filename = ".env"
        ignoreIfMissing = true
    }

    private fun getRequiredValue(key: String): String {
        return System.getenv(key)
            ?: dotenv[key]
            ?: throw IllegalStateException("Missing required environment variable: $key")
    }

    private fun getOptionalValue(key: String): String? {
        return System.getenv(key)
            ?: dotenv[key]
    }

    val dbUrl: String =
        getRequiredValue("DB_URL")

    val dbUser: String =
        getRequiredValue("DB_USER")

    val dbPassword: String =
        getRequiredValue("DB_PASSWORD")

    val dbMigration: String? =
        getOptionalValue("RUN_MIGRATION")

    val dbSeed: String? =
        getOptionalValue("SEED_DATABASE")
}
