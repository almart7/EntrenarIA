package com.alessandra.entrenaria.chat.ViewModel

import com.alessandra.entrenaria.chat.data.model.ChatMessage

// Estado de la UI del chat
data class ChatUiState(
    // lista de mensajes mostrados por pantalla
    val messages: List<ChatMessage> = emptyList()
)