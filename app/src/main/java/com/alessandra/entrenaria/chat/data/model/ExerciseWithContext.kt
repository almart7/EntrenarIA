package com.alessandra.entrenaria.chat.data.model

// exclusivo para pasar datos de entrenamiento a Gemini
data class ExerciseWithContext(
    val periodId: String,
    val dayId: String,
    val exerciseData: Map<String, Any>
)
