package com.entrenaria.models

import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrainingRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun addTrainingPeriod(userId: String, period: TrainingPeriod): String {
        val ref = db.collection("users").document(userId)
            .collection("training_periods").add(period).await()
        return ref.id
    }

    suspend fun addTrainingDay(userId: String, periodId: String, day: TrainingDay): String {
        val ref = db.collection("users").document(userId)
            .collection("training_periods").document(periodId)
            .collection("days").add(day).await()
        return ref.id
    }

    suspend fun addExercise(userId: String, periodId: String, dayId: String, exercise: Exercise): String {
        val ref = db.collection("users").document(userId)
            .collection("training_periods").document(periodId)
            .collection("days").document(dayId)
            .collection("exercises").add(exercise).await()
        return ref.id
    }

    suspend fun getTrainingPeriods(userId: String): List<TrainingPeriod> {
        return db.collection("users").document(userId)
            .collection("training_periods").get().await()
            .toObjects(TrainingPeriod::class.java)
    }

    suspend fun getTrainingDays(userId: String, periodId: String): List<TrainingDay> {
        return db.collection("users").document(userId)
            .collection("training_periods").document(periodId)
            .collection("days").get().await()
            .toObjects(TrainingDay::class.java)
    }

    suspend fun getExercises(userId: String, periodId: String, dayId: String): List<Exercise> {
        return db.collection("users").document(userId)
            .collection("training_periods").document(periodId)
            .collection("days").document(dayId)
            .collection("exercises").get().await()
            .toObjects(Exercise::class.java)
    }
}
