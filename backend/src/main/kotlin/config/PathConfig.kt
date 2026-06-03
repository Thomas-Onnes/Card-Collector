package config

object PathConfig {

    private val workingDirectory =
        System.getProperty("user.dir")

    val resourcePath =
        if (workingDirectory.endsWith("backend")) {
            "src/main/resources"
        } else {
            "backend/src/main/resources"
        }
}