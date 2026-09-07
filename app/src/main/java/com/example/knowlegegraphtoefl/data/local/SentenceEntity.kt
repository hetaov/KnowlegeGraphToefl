package com.example.knowlegegraphtoefl.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.knowlegegraphtoefl.domain.model.Sentence

@Entity(tableName = "sentences")
data class SentenceEntity(
    @PrimaryKey val id: String,
    val text: String,
    val translation: String,
    val category: String,
    val difficulty: Int,
    val isMastered: Boolean,
    val lastReviewed: Long?
)

fun SentenceEntity.toDomain() = Sentence(
    id = id,
    text = text,
    translation = translation,
    category = category,
    difficulty = difficulty,
    isMastered = isMastered,
    lastReviewed = lastReviewed
)

fun Sentence.toEntity() = SentenceEntity(
    id = id,
    text = text,
    translation = translation,
    category = category,
    difficulty = difficulty,
    isMastered = isMastered,
    lastReviewed = lastReviewed
)
