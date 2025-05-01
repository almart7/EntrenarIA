package com.alessandra.entrenaria.data.model

import com.google.firebase.firestore.DocumentId

data class Exercise(
    @DocumentId val id: String = "",
    val name: String = "",
    val sets: List<ExerciseSet> = emptyList(),
    val weight: Float? = null, // Se planifica a nivel del ejercicio
    val notes: String = ""
)
