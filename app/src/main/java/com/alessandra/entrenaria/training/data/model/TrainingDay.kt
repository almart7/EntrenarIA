package com.alessandra.entrenaria.training.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

@Keep
data class TrainingDay(
    @DocumentId val id: String = "",
    val userId: String = "",
    val periodId: String = "",
    val date: Timestamp = Timestamp.now(),
    val label: String = "",
    val notes: String = ""
)

