package com.alessandra.entrenaria.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alessandra.entrenaria.data.model.ChatMessage
import com.alessandra.entrenaria.data.repository.ChatRepository
import com.entrenaria.models.TrainingRepository
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val trainingRepository: TrainingRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = mutableStateOf(ChatUiState())
    val uiState: State<ChatUiState> = _uiState

    init {
        chatRepository.observeMessages(userId) { messages ->
            _uiState.value = _uiState.value.copy(messages = messages)
        }
    }

    fun sendMessage(text: String) {
        val message = ChatMessage(
            sender = "user",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatRepository.sendMessage(userId, message)
        sendQuestionToGemini(text)
    }

    private fun sendQuestionToGemini(question: String) {
        viewModelScope.launch {
            val exercises = trainingRepository.getTrainingDataLastMonth(userId)
            val prompt = chatRepository.buildPromptFromExercises(exercises, question)
            val response = chatRepository.generateGeminiResponse(prompt)

            response?.let {
                chatRepository.sendMessage(
                    userId,
                    ChatMessage(
                        sender = "gemini",
                        text = it,
                        timestamp = System.currentTimeMillis()
                    )
                )

            }
        }
    }
}
