package com.example.knowlegegraphtoefl.domain.repository

import com.example.knowlegegraphtoefl.domain.model.Sentence
import kotlinx.coroutines.flow.Flow

interface SentenceRepository {
    fun getAllSentences(): Flow<List<Sentence>>
    suspend fun getDailyTaskSentences(limit: Int): List<Sentence>
    suspend fun getReviewSentences(limit: Int): List<Sentence>
    suspend fun updateSentenceMastery(id: String, isMastered: Boolean)
    suspend fun insertSentences(sentences: List<Sentence>)
}
