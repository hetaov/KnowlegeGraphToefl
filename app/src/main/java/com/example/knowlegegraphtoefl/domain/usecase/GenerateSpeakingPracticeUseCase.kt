package com.example.knowlegegraphtoefl.domain.usecase

import com.example.knowlegegraphtoefl.data.remote.PromptService
import com.example.knowlegegraphtoefl.domain.model.ChatMessage
import com.example.knowlegegraphtoefl.domain.model.MessageRole
import com.example.knowlegegraphtoefl.domain.model.Sentence
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GenerateSpeakingPracticeUseCase @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val promptService: PromptService
) {
    suspend operator fun invoke(targetSentences: List<Sentence>): ChatMessage {
        val prompt = promptService.buildPracticeStartPrompt(targetSentences)
        val response = generativeModel.generateContent(prompt)
        
        return ChatMessage(
            role = MessageRole.ASSISTANT,
            content = response.text ?: "Sorry, I couldn't generate a response."
        )
    }

    suspend fun getNextResponse(
        history: List<ChatMessage>,
        targetSentences: List<Sentence>,
        userMessage: String
    ): ChatMessage {
        val prompt = promptService.buildResponsePrompt(history, targetSentences, userMessage)
        val response = generativeModel.generateContent(prompt)
        
        return ChatMessage(
            role = MessageRole.ASSISTANT,
            content = response.text ?: "I'm having trouble responding right now."
        )
    }
}
