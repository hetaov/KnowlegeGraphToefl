package com.example.knowlegegraphtoefl.domain.model

data class Sentence(
    val id: String,
    val text: String,
    val translation: String,
    val category: String,
    val difficulty: Int,
    val isMastered: Boolean = false,
    val lastReviewed: Long? = null
)
