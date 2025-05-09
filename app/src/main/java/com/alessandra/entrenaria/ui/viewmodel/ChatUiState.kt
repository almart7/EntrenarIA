package com.alessandra.entrenaria.ui.viewmodel

import com.alessandra.entrenaria.data.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList()
)