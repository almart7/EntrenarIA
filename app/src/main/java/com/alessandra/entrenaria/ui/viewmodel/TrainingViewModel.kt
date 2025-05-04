package com.alessandra.entrenaria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.entrenaria.models.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingViewModel(
    private val repository: TrainingRepository,
    private val userId: String
) : ViewModel() {

    private val _trainingPeriods = MutableStateFlow<List<TrainingPeriod>>(emptyList())
    val trainingPeriods: StateFlow<List<TrainingPeriod>> = _trainingPeriods.asStateFlow()

    private val _trainingDays = MutableStateFlow<List<TrainingDay>>(emptyList())
    val trainingDays: StateFlow<List<TrainingDay>> = _trainingDays.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    fun loadTrainingPeriods() {
        viewModelScope.launch {
            _trainingPeriods.value = repository.getTrainingPeriods(userId)
        }
    }

    fun loadTrainingDays(periodId: String) {
        viewModelScope.launch {
            _trainingDays.value = repository.getTrainingDays(userId, periodId)
        }
    }

    fun loadExercises(dayId: String) {
        viewModelScope.launch {
            _exercises.value = repository.getExercises(userId, dayId)
        }
    }

    fun addTrainingPeriod(period: TrainingPeriod) {
        viewModelScope.launch {
            repository.addTrainingPeriod(period.copy(userId = userId))
            loadTrainingPeriods()
        }
    }

    fun addTrainingDay(day: TrainingDay) {
        viewModelScope.launch {
            repository.addTrainingDay(day.copy(userId = userId))
            loadTrainingDays(day.periodId)
        }
    }

    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.addExercise(exercise.copy(userId = userId))
            loadExercises(exercise.dayId)
        }
    }

    fun deleteTrainingPeriodWithChildren(periodId: String) {
        viewModelScope.launch {
            repository.deleteTrainingPeriodWithChildren(userId, periodId)
            loadTrainingPeriods()
        }
    }

    fun deleteTrainingDay(dayId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTrainingDay(dayId)
                // Si quieres refrescar los días visibles:
                _trainingDays.value = _trainingDays.value.filterNot { it.id == dayId }
            } catch (e: Exception) {
                // Manejo de error opcional
            }
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(exerciseId)
                _exercises.value = _exercises.value.filterNot { it.id == exerciseId }
            } catch (e: Exception) {
                // Manejo opcional: puedes mostrar un error en la UI
            }
        }
    }


}
