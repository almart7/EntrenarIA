package com.alessandra.entrenaria.chat.data.model

data class ExerciseWithContext(
    val periodId: String,
    val dayId: String,
    val exerciseData: Map<String, Any>
)
