package com.example.knowlegegraphtoefl.domain.model

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class ChatMessage(
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
