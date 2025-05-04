package com.alessandra.entrenaria.data.model

data class ExerciseSet(
    val targetRepsMin: Int = 0,
    val targetRepsMax: Int = 0,
    val actualReps: Int? = null
) {
    val wasCompleted: Boolean
        get() = actualReps != null

    fun isWithinTarget(): Boolean {
        return actualReps != null && actualReps in targetRepsMin..targetRepsMax
    }
}
