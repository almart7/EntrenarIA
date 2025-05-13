package com.entrenaria.models

import android.util.Log
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.data.model.ExerciseWithContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun getTrainingPeriodById(periodId: String): TrainingPeriod? {
        val snapshot = db.collection("training_periods")
            .document(periodId)
            .get()
            .await()

        return snapshot.toObject(TrainingPeriod::class.java)?.copy(id = snapshot.id)
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
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(Exercise::class.java)
    }

    suspend fun getExerciseById(id: String): Exercise? {
        val snapshot = db.collection("exercises")
            .document(id)
            .get()
            .await()

        return snapshot.toObject(Exercise::class.java)?.copy(id = snapshot.id)
    }


    /*suspend fun getExercisesForPeriod(userId: String, periodId: String, name: String? = null): List<Exercise> {
        var query = db.collection("exercises")
            .whereEqualTo("userId", userId)
            .whereEqualTo("periodId", periodId)

        if (name != null) {
            query = query.whereEqualTo("name", name)
        }

        return query.get().await().toObjects(Exercise::class.java)
    }*/

    suspend fun getExerciseNamesForUser(userId: String): List<String> {
        return db.collection("exercises")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("name") }
            .distinct()
    }

    // consulta entrenamientos de los ultimos 30 dias para chat con GeminI
    suspend fun getTrainingDataLastMonth(userId: String): List<ExerciseWithContext> {
        val db = FirebaseFirestore.getInstance()
        val result = mutableListOf<ExerciseWithContext>()
        val oneMonthAgo = Timestamp(Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000))

        Log.d("TrainingDebug", "📆 Fecha límite: $oneMonthAgo")

        // Obtener todos los períodos de entrenamiento del usuario
        val periods = db.collection("training_periods")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        for (period in periods.documents) {
            val periodId = period.id

            // Obtener días del período con fecha dentro del último mes
            val days = db.collection("training_days")
                .whereEqualTo("periodId", periodId)
                .whereGreaterThanOrEqualTo("date", oneMonthAgo)
                .get()
                .await()


            for (day in days.documents) {
                val dayId = day.id

                // Obtener ejercicios del día
                val exercises = db.collection("exercises")
                    .whereEqualTo("dayId", dayId)
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

    // --------- ACTUALIZACIÓN ---------
    suspend fun updateExercise(exercise: Exercise) {
        db.collection("exercises")
            .document(exercise.id)
            .set(exercise)
            .await() // 👈 importante para esperar la operación
    }

    suspend fun updateTrainingDay(day: TrainingDay) {
        db.collection("training_days")
            .document(day.id)
            .set(day)
            .await()
    }

    suspend fun updateTrainingPeriod(period: TrainingPeriod) {
        db.collection("training_periods")
            .document(period.id)
            .set(period)
            .await()
    }


}
