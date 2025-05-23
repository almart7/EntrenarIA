package com.alessandra.entrenaria.chat.data.model

import androidx.annotation.Keep

@Keep
data class ChatMessage(
    val messageId: String = "",
    val sender: String = "", // "user" o "gemini"
    val text: String = "",
    val timestamp: Long = 0L
)