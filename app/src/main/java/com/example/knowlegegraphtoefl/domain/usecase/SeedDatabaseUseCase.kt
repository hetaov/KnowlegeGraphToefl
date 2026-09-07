package com.example.knowlegegraphtoefl.domain.usecase

import com.example.knowlegegraphtoefl.domain.model.Sentence
import com.example.knowlegegraphtoefl.domain.repository.SentenceRepository
import java.util.UUID
import javax.inject.Inject

class SeedDatabaseUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke() {
        val samples = listOf(
            Sentence(
                id = UUID.randomUUID().toString(),
                text = "The theory of evolution explains the diversity of life through natural selection.",
                translation = "进化论通过自然选择解释了生命的多样性。",
                category = "Biology",
                difficulty = 3
            ),
            Sentence(
                id = UUID.randomUUID().toString(),
                text = "Economic growth is often measured by the increase in a country's Gross Domestic Product.",
                translation = "经济增长通常通过一个国家国内生产总值的增加来衡量。",
                category = "Economics",
                difficulty = 2
            ),
            Sentence(
                id = UUID.randomUUID().toString(),
                text = "Photosynthesis is the process by which green plants use sunlight to synthesize nutrients.",
                translation = "光合作用是绿色植物利用阳光合成养分的过程。",
                category = "Biology",
                difficulty = 3
            ),
            Sentence(
                id = UUID.randomUUID().toString(),
                text = "The Industrial Revolution marked a major turning point in history, influencing almost every aspect of daily life.",
                translation = "工业革命标志着历史的一个重要转折点，几乎影响了日常生活的方方面面。",
                category = "History",
                difficulty = 4
            ),
            Sentence(
                id = UUID.randomUUID().toString(),
                text = "Renewable energy sources, such as solar and wind power, are essential for sustainable development.",
                translation = "太阳能和风能等可再生能源对可持续发展至关重要。",
                category = "Environmental Science",
                difficulty = 2
            )
        )
        repository.insertSentences(samples)
    }
}
