package com.alessandra.entrenaria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alessandra.entrenaria.data.repository.ChatRepository
import com.entrenaria.models.TrainingRepository

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val trainingRepository: TrainingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(chatRepository, trainingRepository, userId) as T
    }
}
