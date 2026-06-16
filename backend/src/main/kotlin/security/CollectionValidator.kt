package security

object CollectionValidator {

    private val collectionNameRegex =
        Regex("^[\\p{L}0-9 _-]+$")

    fun validateCollectionName(collectionName: String): String {
        val value = collectionName.trim()

        if (value.isBlank()) {
            throw IllegalArgumentException("Collection name is required")
        }

        if (value.length < 3) {
            throw IllegalArgumentException("Collection name must be at least 3 characters")
        }

        if (value.length > 50) {
            throw IllegalArgumentException("Collection name may not be longer than 50 characters")
        }

        if (!collectionNameRegex.matches(value)) {
            throw IllegalArgumentException("Collection name may only contain letters, numbers, spaces, _ and -")
        }

        return value
    }

    fun normalizeGameType(gameType: String): String {
        return when (gameType.trim().lowercase()) {
            "pokemon", "pokémon" -> "pokemon"
            "mtg", "magic", "magic the gathering", "magic_the_gathering" -> "mtg"
            else -> throw IllegalArgumentException("Invalid collection type")
        }
    }
}