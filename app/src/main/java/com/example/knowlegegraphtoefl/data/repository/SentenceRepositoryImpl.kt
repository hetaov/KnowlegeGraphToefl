package com.example.knowlegegraphtoefl.data.repository

import com.example.knowlegegraphtoefl.data.local.SentenceDao
import com.example.knowlegegraphtoefl.data.local.toDomain
import com.example.knowlegegraphtoefl.data.local.toEntity
import com.example.knowlegegraphtoefl.domain.model.Sentence
import com.example.knowlegegraphtoefl.domain.repository.SentenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SentenceRepositoryImpl @Inject constructor(
    private val sentenceDao: SentenceDao
) : SentenceRepository {
    override fun getAllSentences(): Flow<List<Sentence>> {
        return sentenceDao.getAllSentences().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDailyTaskSentences(limit: Int): List<Sentence> {
        return sentenceDao.getUnmasteredSentences(limit).map { it.toDomain() }
    }

    override suspend fun getReviewSentences(limit: Int): List<Sentence> {
        return sentenceDao.getMasteredSentencesForReview(limit).map { it.toDomain() }
    }

    override suspend fun updateSentenceMastery(id: String, isMastered: Boolean) {
        sentenceDao.updateMastery(id, isMastered, System.currentTimeMillis())
    }

    override suspend fun insertSentences(sentences: List<Sentence>) {
        sentenceDao.insertSentences(sentences.map { it.toEntity() })
    }
}
