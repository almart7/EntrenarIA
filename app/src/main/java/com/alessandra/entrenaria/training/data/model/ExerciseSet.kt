package com.alessandra.entrenaria.training.data.model

import androidx.annotation.Keep

@Keep
data class ExerciseSet(
    val targetRepsMin: Int = 0,
    val targetRepsMax: Int = 0,
    val actualReps: Int? = null
)
