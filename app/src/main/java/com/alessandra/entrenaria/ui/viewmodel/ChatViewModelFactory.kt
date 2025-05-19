package com.alessandra.entrenaria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alessandra.entrenaria.data.repository.ChatRepository
import com.entrenaria.models.TrainingRepository

// Fábrica personalizada para crear un ChatViewModel con parámetros
// por defecto, solo se pueden crear ViewModels sin parámetros.
class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val trainingRepository: TrainingRepository,
    private val userId: String
) : ViewModelProvider.Factory {
    // Esta función se encarga de construir el ViewModel con los argumentos necesarios
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Devuelve una instancia de ChatViewModel con inyección manual de dependencias
        return ChatViewModel(chatRepository, trainingRepository, userId) as T
    }
}
