package com.entrenaria.models

import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TrainingRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // --------- CREACIÓN ---------

    suspend fun addTrainingPeriod(period: TrainingPeriod): String {
        val ref = db.collection("training_periods").add(period).await()
        return ref.id
    }

    suspend fun addTrainingDay(day: TrainingDay): String {
        val ref = db.collection("training_days").add(day).await()
        return ref.id
    }

    suspend fun addExercise(exercise: Exercise): String {
        val ref = db.collection("exercises").add(exercise).await()
        return ref.id
    }

    // --------- LECTURA ---------

    suspend fun getTrainingPeriods(userId: String): List<TrainingPeriod> {
        return db.collection("training_periods")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(TrainingPeriod::class.java)
    }

    suspend fun getTrainingDays(userId: String, periodId: String): List<TrainingDay> {
        return db.collection("training_days")
            .whereEqualTo("userId", userId)
            .whereEqualTo("periodId", periodId)
            .get()
            .await()
            .toObjects(TrainingDay::class.java)
    }

    suspend fun getExercises(userId: String, dayId: String): List<Exercise> {
        return db.collection("exercises")
            .whereEqualTo("userId", userId)
            .whereEqualTo("dayId", dayId)
            .get()
            .await()
            .toObjects(Exercise::class.java)
    }

    suspend fun getExercisesForPeriod(userId: String, periodId: String, name: String? = null): List<Exercise> {
        var query = db.collection("exercises")
            .whereEqualTo("userId", userId)
            .whereEqualTo("periodId", periodId)

        if (name != null) {
            query = query.whereEqualTo("name", name)
        }

        return query.get().await().toObjects(Exercise::class.java)
    }

    // --------- BORRADO ---------

    suspend fun deleteTrainingPeriod(periodId: String) {
        db.collection("training_periods").document(periodId).delete().await()
    }

    suspend fun deleteTrainingDay(dayId: String) {
        db.collection("training_days").document(dayId).delete().await()
    }

    suspend fun deleteExercise(exerciseId: String) {
        db.collection("exercises").document(exerciseId).delete().await()
    }

    suspend fun deleteTrainingPeriodWithChildren(userId: String, periodId: String) {
        // Borrar ejercicios relacionados
        val exercises = db.collection("exercises")
            .whereEqualTo("userId", userId)
            .whereEqualTo("periodId", periodId)
            .get().await()

        for (doc in exercises.documents) {
            doc.reference.delete()
        }

        // Borrar días relacionados
        val days = db.collection("training_days")
            .whereEqualTo("userId", userId)
            .whereEqualTo("periodId", periodId)
            .get().await()

        for (doc in days.documents) {
            doc.reference.delete()
        }

        // Borrar el periodo
        deleteTrainingPeriod(periodId)
    }
}
