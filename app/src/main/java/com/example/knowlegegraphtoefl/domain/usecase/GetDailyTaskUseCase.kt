package com.example.knowlegegraphtoefl.domain.usecase

import com.example.knowlegegraphtoefl.domain.model.DailyTask
import com.example.knowlegegraphtoefl.domain.repository.SentenceRepository
import java.util.UUID
import javax.inject.Inject

class GetDailyTaskUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(): DailyTask {
        val newSentences = repository.getDailyTaskSentences(5)
        val reviewSentences = repository.getReviewSentences(3)
        
        return DailyTask(
            id = UUID.randomUUID().toString(),
            date = System.currentTimeMillis(),
            sentences = newSentences + reviewSentences
        )
    }
}
