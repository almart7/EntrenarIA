package com.alessandra.entrenaria.training.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

@Keep
data class TrainingPeriod(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    val type: String = "custom",
    val notes: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
