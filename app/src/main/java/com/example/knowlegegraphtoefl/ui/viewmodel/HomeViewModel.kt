package com.example.knowlegegraphtoefl.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.knowlegegraphtoefl.domain.model.DailyTask
import com.example.knowlegegraphtoefl.domain.usecase.GetDailyTaskUseCase
import com.example.knowlegegraphtoefl.domain.usecase.SeedDatabaseUseCase
import com.example.knowlegegraphtoefl.domain.usecase.UpdateLearningProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyTaskUseCase: GetDailyTaskUseCase,
    private val updateLearningProgressUseCase: UpdateLearningProgressUseCase,
    private val seedDatabaseUseCase: SeedDatabaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadDailyTask()
    }

    private fun loadDailyTask() {
        viewModelScope.launch {
            try {
                var task = getDailyTaskUseCase()
                if (task.sentences.isEmpty()) {
                    seedDatabaseUseCase()
                    task = getDailyTaskUseCase()
                }
                _uiState.value = HomeUiState.Success(task)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun markAsMastered(sentenceId: String) {
        viewModelScope.launch {
            updateLearningProgressUseCase(sentenceId, true)
            loadDailyTask() // Refresh
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val task: DailyTask) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
