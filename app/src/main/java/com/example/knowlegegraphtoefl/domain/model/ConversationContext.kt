package com.example.knowlegegraphtoefl.domain.model

data class ConversationContext(
    val targetSentences: List<Sentence>,
    val history: List<ChatMessage>
)
