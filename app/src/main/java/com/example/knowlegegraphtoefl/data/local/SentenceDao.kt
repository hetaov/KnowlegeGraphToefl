package com.example.knowlegegraphtoefl.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceDao {
    @Query("SELECT * FROM sentences")
    fun getAllSentences(): Flow<List<SentenceEntity>>

    @Query("SELECT * FROM sentences WHERE isMastered = 0 LIMIT :limit")
    suspend fun getUnmasteredSentences(limit: Int): List<SentenceEntity>

    @Query("SELECT * FROM sentences WHERE isMastered = 1 ORDER BY lastReviewed ASC LIMIT :limit")
    suspend fun getMasteredSentencesForReview(limit: Int): List<SentenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentences(sentences: List<SentenceEntity>)

    @Query("UPDATE sentences SET isMastered = :isMastered, lastReviewed = :timestamp WHERE id = :id")
    suspend fun updateMastery(id: String, isMastered: Boolean, timestamp: Long)
}
