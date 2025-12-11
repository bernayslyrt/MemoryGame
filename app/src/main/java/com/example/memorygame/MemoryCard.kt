package com.example.memorygame

data class MemoryCard(
    val id: Int,
    val emoji: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)