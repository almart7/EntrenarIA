package com.alessandra.entrenaria.ui.viewmodel

import com.alessandra.entrenaria.data.model.ChatMessage

// Estado de la UI del chat
data class ChatUiState(
    // lista de mensajes mostrados por pantalla
    val messages: List<ChatMessage> = emptyList()
)