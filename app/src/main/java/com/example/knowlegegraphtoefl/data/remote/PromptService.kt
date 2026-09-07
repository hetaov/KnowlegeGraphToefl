package com.example.knowlegegraphtoefl.data.remote

import com.example.knowlegegraphtoefl.domain.model.ChatMessage
import com.example.knowlegegraphtoefl.domain.model.Sentence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptService @Inject constructor() {

    fun buildPracticeStartPrompt(targetSentences: List<Sentence>): String {
        val sentencesList = targetSentences.joinToString("\n") { "- ${it.text} (Context: ${it.category})" }
        
        return """
            You are a helpful TOEFL speaking partner. 
            Your goal is to help the user practice specific academic English sentences in a natural conversation.
            
            TARGET SENTENCES:
            $sentencesList
            
            INSTRUCTIONS:
            1. Start by greeting the user and setting up a scenario (e.g., a university campus, a library, or a lab) related to these sentences.
            2. You MUST use at least one of these sentences in your opening response.
            3. Ask an open-ended question to get the user to talk.
            4. Throughout the conversation, try to elicit these sentences from the user or model them yourself.
            
            Please output only your conversational response.
        """.trimIndent()
    }

    fun buildResponsePrompt(
        history: List<ChatMessage>,
        targetSentences: List<Sentence>,
        userMessage: String
    ): String {
        val sentencesList = targetSentences.joinToString("\n") { "- ${it.text}" }
        val historyText = history.joinToString("\n") { "${it.role}: ${it.content}" }

        return """
            Continue the TOEFL speaking practice conversation.
            
            TARGET SENTENCES STILL TO BE PRACTICED OR REINFORCED:
            $sentencesList
            
            CONVERSATION HISTORY:
            $historyText
            
            USER SAID:
            $userMessage
            
            INSTRUCTIONS:
            1. Respond naturally to the user.
            2. If they made a grammar mistake, gently correct them.
            3. Try to integrate another target sentence if it fits.
            4. Keep the academic tone appropriate for TOEFL.
            
            Output only your response.
        """.trimIndent()
    }
}
