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

    private fun getOptionalBoolean(key: String): Boolean {
        return getOptionalValue(key)?.toBoolean() ?: false
    }

    val dbUrl: String =
        getRequiredValue("DB_URL")

    val dbUser: String =
        getRequiredValue("DB_USER")

    val dbPassword: String =
        getRequiredValue("DB_PASSWORD")

    val dbMigration: Boolean =
        getOptionalBoolean("RUN_MIGRATION")

    val dbSeed: Boolean =
        getOptionalBoolean("SEED_DATABASE")

    val importAllPokemonSets: Boolean =
        getOptionalBoolean("IMPORT_ALL_POKEMON_SETS")

    val selectedPokemonSets: List<String> =
        getOptionalValue("SELECTED_POKEMON_SETS")
            ?.also {
                println("SELECTED_POKEMON_SETS uit .env: $it")
            }
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()

}