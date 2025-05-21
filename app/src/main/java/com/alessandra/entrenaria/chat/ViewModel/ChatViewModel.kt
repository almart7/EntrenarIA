package com.alessandra.entrenaria.chat.ViewModel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alessandra.entrenaria.chat.data.model.ChatMessage
import com.alessandra.entrenaria.chat.data.repository.ChatRepository
import com.alessandra.entrenaria.training.data.repository.TrainingRepository
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val trainingRepository: TrainingRepository,
    private val userId: String
) : ViewModel() {

    // Estado interno del ViewModel con los mensajes del chat
    private val _uiState = mutableStateOf(ChatUiState())
    val uiState: State<ChatUiState> = _uiState // expuesto para la UI

    // Indica si es el primer mensaje de la sesión
    private var isFirstQuestion = true

    init {
        // Se suscribe a Firebase para recibir nuevos mensajes en tiempo real
        chatRepository.observeMessages(userId) { messages ->
            _uiState.value = _uiState.value.copy(messages = messages)
        }
    }


    /**
     * Se llama cuando el usuario envía un mensaje desde la UI.
     * Guarda el mensaje en Firebase y lanza la petición a Gemini.
     */
    fun sendMessage(text: String) {
        val message = ChatMessage(
            sender = "user",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatRepository.sendMessage(userId, message)
        sendQuestionToGemini(text)
    }

    /**
     * Gestiona el envío de la pregunta a Gemini.
     * Solo en la primera pregunta de la sesión incluye los datos de entrenamiento del último mes.
     */
    private fun sendQuestionToGemini(question: String) {
        viewModelScope.launch {
            val prompt = if (isFirstQuestion) {
                // Obtiene ejercicios del último mes desde Firestore
                val exercises = trainingRepository.getTrainingDataLastMonth(userId)
                // Ya se ha realizado la primera pregunta (a partir de aquí)
                isFirstQuestion = false
                // Crea el prompt con contexto de entrenamiento
                chatRepository.buildPromptFromExercises(exercises, question)
            } else {
                question
            }

            // Si hay respuesta, la guarda como mensaje de Gemini
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
