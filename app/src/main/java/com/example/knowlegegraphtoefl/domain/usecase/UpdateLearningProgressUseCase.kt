package com.example.knowlegegraphtoefl.domain.usecase

import com.example.knowlegegraphtoefl.domain.repository.SentenceRepository
import javax.inject.Inject

class UpdateLearningProgressUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(sentenceId: String, isMastered: Boolean) {
        repository.updateSentenceMastery(sentenceId, isMastered)
    }
}
