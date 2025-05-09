package com.entrenaria.models

import android.util.Log
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.data.model.ExerciseWithContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

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

    // consulta entrenamientos de los ultimos 30 dias para chat con GeminI
    suspend fun getTrainingDataLastMonth(userId: String): List<ExerciseWithContext> {
        val db = FirebaseFirestore.getInstance()
        val result = mutableListOf<ExerciseWithContext>()
        val oneMonthAgo = Timestamp(Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000))

        Log.d("TrainingDebug", "📆 Fecha límite: $oneMonthAgo")

        // Obtener todos los períodos de entrenamiento del usuario
        val periods = db.collection("training_periods")
            .whereEqualTo("user_id", userId)
            .get()
            .await()
        Log.d("TrainingDebug", "📂 Periodos encontrados: ${periods.size()}")

        for (period in periods.documents) {
            val periodId = period.id
            Log.d("TrainingDebug", "📁 Periodo: $periodId")

            // Obtener días del período con fecha dentro del último mes
            val days = db.collection("training_days")
                .whereEqualTo("period_id", periodId)
                .whereGreaterThanOrEqualTo("date", oneMonthAgo)
                .get()
                .await()

            Log.d("TrainingDebug", "📄 Días recientes en $periodId: ${days.size()}")

            for (day in days.documents) {
                val dayId = day.id
                val rawDate = day.getTimestamp("date")
                Log.d("TrainingDebug", "📆 Día válido: $dayId, fecha: $rawDate")

                // Obtener ejercicios del día
                val exercises = db.collection("exercises")
                    .whereEqualTo("day_id", dayId)
                    .get()
                    .await()

                Log.d("TrainingDebug", "💪 Ejercicios en $dayId: ${exercises.size()}")

                for (exercise in exercises.documents) {
                    val data = exercise.data ?: continue
                    Log.d("TrainingDebug", "💥 Ejercicio: ${data["name"]} (sets: ${data["sets"]})")

                    result.add(
                        ExerciseWithContext(
                            periodId = periodId,
                            dayId = dayId,
                            exerciseData = data
                        )
                    )
                }
            }
        }

        Log.d("TrainingDebug", "✅ Total ejercicios encontrados: ${result.size}")
        return result
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
