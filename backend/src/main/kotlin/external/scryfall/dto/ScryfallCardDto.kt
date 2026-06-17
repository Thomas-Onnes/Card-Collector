package external.scryfall.dto
import com.fasterxml.jackson.annotation.JsonProperty

data class ScryfallCardDto (
    val id: String, //this is the scryfall ID
    val name: String,

    @JsonProperty("set")
    val setCode: String,

    @JsonProperty("set_name")
    val setName: String,

    val rarity: String,

    @JsonProperty("mana_cost")
    val manaCost: String?,

    @JsonProperty("type_line")
    val typeLine: String?,

    @JsonProperty("artist")
    val illustrator: String?,

  //  @SerialName("image_uris")
    // val image: String?
)