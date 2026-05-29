package config

import io.github.cdimascio.dotenv.dotenv

object Environment {

    private val dotenv = dotenv{directory = "./backend"}

    val dbUrl: String =
        dotenv["DB_URL"]

    val dbUser: String =
        dotenv["DB_USER"]

    val dbPassword: String =
        dotenv["DB_PASSWORD"]
}
