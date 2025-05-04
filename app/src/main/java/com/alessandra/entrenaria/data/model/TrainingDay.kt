package com.alessandra.entrenaria.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TrainingDay(
    @DocumentId val id: String = "",
    val userId: String = "",          // 🔧 Necesario para saber de qué usuario es
    val periodId: String = "",        // 🔧 Relación con el TrainingPeriod
    val date: Timestamp = Timestamp.now(),
    val label: String = "",
    val notes: String = ""
)

