package config

import io.github.cdimascio.dotenv.dotenv

object Environment {

    private val dotenv = dotenv{
        directory = "."
        filename = ".env"
    }

    val dbUrl: String =
        dotenv["DB_URL"]

    val dbUser: String =
        dotenv["DB_USER"]

    val dbPassword: String =
        dotenv["DB_PASSWORD"]

    val dbMigration: String? =
        dotenv["RUN_MIGRATION"]

    val dbSeed: String? =
        dotenv["SEED_DATABASE"]

}
