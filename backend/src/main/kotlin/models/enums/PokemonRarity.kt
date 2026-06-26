package models.enums

enum class PokemonRarity(val apiValue: String) {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    RARE_HOLO("Rare Holo"),
    RARE_HOLO_EX("Rare Holo EX"),
    RARE_HOLO_GX("Rare Holo GX"),
    ULTRA_RARE("Ultra Rare"),
    SECRET_RARE("Secret Rare"),
    DOUBLE_RARE("Double Rare"),
    ILLUSTRATION_RARE("Illustration Rare"),
    SPECIAL_ILLUSTRATION_RARE("Special Illustration Rare"),
    HYPER_RARE("Hyper Rare"),
    PROMO("Promo"),
    UNKNOWN("Unknown")
}