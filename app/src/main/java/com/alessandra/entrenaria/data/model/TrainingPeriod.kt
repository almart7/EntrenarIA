package com.alessandra.entrenaria.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class TrainingPeriod(
    @DocumentId val id: String = "",
    val title: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp? = null,
    val type: String = "custom", // "weekly", "monthly", etc.
    val notes: String = "",
    val createdAt: Timestamp = Timestamp.now()
)