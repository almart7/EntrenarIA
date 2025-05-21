package com.alessandra.entrenaria.training.data.model

data class ExerciseSet(
    val targetRepsMin: Int = 0,
    val targetRepsMax: Int = 0,
    val actualReps: Int? = null
)
