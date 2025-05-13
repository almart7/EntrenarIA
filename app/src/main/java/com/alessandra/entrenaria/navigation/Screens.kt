package com.alessandra.entrenaria.navigation

import kotlinx.serialization.Serializable

@Serializable
object Initial

@Serializable
object Login

@Serializable
object SignUp

@Serializable
object Profile

@Serializable
object Home

@Serializable
data class TrainingDays(val periodId: String)

@Serializable
data class ExerciseList(val periodId: String, val dayId: String)

@Serializable
data class NewExercise(
    val periodId: String,
    val dayId: String,
    val exerciseId: String? = null
)

@Serializable
object Chat
