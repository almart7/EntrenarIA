package com.alessandra.entrenaria.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Exercise(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dayId: String = "",
    val periodId: String = "",
    val name: String = "",
    val sets: List<ExerciseSet> = emptyList(),
    val weight: Float? = null,
    val instructions: String = "",
    val notes: String = "",
    val createdAt: Timestamp = Timestamp.now()
)


