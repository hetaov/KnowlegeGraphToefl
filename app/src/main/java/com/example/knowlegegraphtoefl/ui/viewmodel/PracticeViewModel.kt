package com.example.knowlegegraphtoefl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.knowlegegraphtoefl.domain.model.ChatMessage
import com.example.knowlegegraphtoefl.domain.model.MessageRole
import com.example.knowlegegraphtoefl.domain.model.Sentence
import com.example.knowlegegraphtoefl.domain.usecase.GenerateSpeakingPracticeUseCase
import com.example.knowlegegraphtoefl.ui.components.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val generateSpeakingPracticeUseCase: GenerateSpeakingPracticeUseCase,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    fun startPractice(targetSentences: List<Sentence>) {
        viewModelScope.launch {
            val initialMessage = generateSpeakingPracticeUseCase(targetSentences)
            _chatMessages.value = listOf(initialMessage)
            ttsManager.speak(initialMessage.content)
        }
    }

    fun sendMessage(content: String, targetSentences: List<Sentence>) {
        val userMessage = ChatMessage(MessageRole.USER, content)
        val currentMessages = _chatMessages.value
        _chatMessages.value = currentMessages + userMessage
        
        viewModelScope.launch {
            val response = generateSpeakingPracticeUseCase.getNextResponse(
                history = currentMessages,
                targetSentences = targetSentences,
                userMessage = content
            )
            _chatMessages.value = _chatMessages.value + response
            ttsManager.speak(response.content)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}
