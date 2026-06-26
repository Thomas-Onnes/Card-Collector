package com.example.cardcollector.models

data class CollectionCardsResponse(
    val cards: List<CollectionCardItem>,
    val totalPrice: Double
)