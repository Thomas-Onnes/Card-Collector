package models.enums

enum class PokemonRarity(val displayName: String) {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    RARE_HOLO("Rare Holo"),
    RARE_HOLO_EX("Rare Holo EX"),
    RARE_HOLO_GX("Rare Holo GX"),
    RARE_HOLO_V("Rare Holo V"),
    RARE_HOLO_VMAX("Rare Holo VMAX"),
    RARE_HOLO_VSTAR("Rare Holo VSTAR"),
    ULTRA_RARE("Ultra Rare"),
    SECRET_RARE("Secret Rare"),
    DOUBLE_RARE("Double Rare"),
    ILLUSTRATION_RARE("Illustration Rare"),
    SPECIAL_ILLUSTRATION_RARE("Special Illustration Rare"),
    HYPER_RARE("Hyper Rare"),
    AMAZING_RARE("Amazing Rare"),
    RADIANT_RARE("Radiant Rare"),
    ACE_SPEC_RARE("ACE SPEC Rare"),
    PROMO("Promo"),
    UNKNOWN("Unknown");

    companion object {
        fun fromApiValue(value: String?): PokemonRarity {
            val normalized = value
                ?.trim()
                ?.uppercase()
                ?.replace(" ", "_")
                ?.replace("-", "_")
                ?: return UNKNOWN

            return when {
                normalized == "COMMON" -> COMMON
                normalized == "UNCOMMON" -> UNCOMMON
                normalized == "RARE" -> RARE
                normalized == "RARE_HOLO" -> RARE_HOLO
                normalized == "RARE_HOLO_EX" -> RARE_HOLO_EX
                normalized == "RARE_HOLO_GX" -> RARE_HOLO_GX
                normalized == "RARE_HOLO_V" -> RARE_HOLO_V
                normalized == "RARE_HOLO_VMAX" -> RARE_HOLO_VMAX
                normalized == "RARE_HOLO_VSTAR" -> RARE_HOLO_VSTAR
                normalized == "ULTRA_RARE" -> ULTRA_RARE
                normalized == "SECRET_RARE" || normalized == "RARE_SECRET" -> SECRET_RARE
                normalized == "DOUBLE_RARE" -> DOUBLE_RARE
                normalized == "ILLUSTRATION_RARE" -> ILLUSTRATION_RARE
                normalized == "SPECIAL_ILLUSTRATION_RARE" -> SPECIAL_ILLUSTRATION_RARE
                normalized == "HYPER_RARE" -> HYPER_RARE
                normalized == "AMAZING_RARE" -> AMAZING_RARE
                normalized == "RADIANT_RARE" -> RADIANT_RARE
                normalized == "ACE_SPEC_RARE" -> ACE_SPEC_RARE
                normalized.contains("PROMO") -> PROMO
                normalized.contains("SPECIAL_ILLUSTRATION") -> SPECIAL_ILLUSTRATION_RARE
                normalized.contains("ILLUSTRATION") -> ILLUSTRATION_RARE
                normalized.contains("HYPER") -> HYPER_RARE
                normalized.contains("DOUBLE") -> DOUBLE_RARE
                normalized.contains("SECRET") -> SECRET_RARE
                normalized.contains("ULTRA") -> ULTRA_RARE
                normalized.contains("RADIANT") -> RADIANT_RARE
                normalized.contains("AMAZING") -> AMAZING_RARE
                normalized.contains("ACE_SPEC") -> ACE_SPEC_RARE
                normalized.contains("HOLO") && normalized.contains("VSTAR") -> RARE_HOLO_VSTAR
                normalized.contains("HOLO") && normalized.contains("VMAX") -> RARE_HOLO_VMAX
                normalized.contains("HOLO") && normalized.contains("_V") -> RARE_HOLO_V
                normalized.contains("HOLO") && normalized.contains("GX") -> RARE_HOLO_GX
                normalized.contains("HOLO") && normalized.contains("EX") -> RARE_HOLO_EX
                normalized.contains("HOLO") -> RARE_HOLO
                normalized.contains("UNCOMMON") -> UNCOMMON
                normalized.contains("COMMON") -> COMMON
                normalized.contains("RARE") -> RARE
                else -> UNKNOWN
            }
        }
    }
}