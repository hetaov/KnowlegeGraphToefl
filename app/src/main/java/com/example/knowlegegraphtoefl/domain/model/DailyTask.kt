package com.example.knowlegegraphtoefl.domain.model

data class DailyTask(
    val id: String,
    val date: Long,
    val sentences: List<Sentence>,
    val isCompleted: Boolean = false
)
