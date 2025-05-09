package com.alessandra.entrenaria.data.model

data class ExerciseWithContext(
    val periodId: String,
    val dayId: String,
    val exerciseData: Map<String, Any>
)
