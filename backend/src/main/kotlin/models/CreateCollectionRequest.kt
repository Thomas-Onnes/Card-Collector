package models

data class CreateCollectionRequest(
    val collectionName: String,
    val gameType: String
)