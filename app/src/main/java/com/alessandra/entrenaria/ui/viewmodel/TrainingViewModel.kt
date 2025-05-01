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

class TrainingViewModel(private val repository: TrainingRepository, private val userId: String) : ViewModel() {

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

    fun loadExercises(periodId: String, dayId: String) {
        viewModelScope.launch {
            _exercises.value = repository.getExercises(userId, periodId, dayId)
        }
    }

    fun addTrainingPeriod(period: TrainingPeriod) {
        viewModelScope.launch {
            repository.addTrainingPeriod(userId, period)
            loadTrainingPeriods()
        }
    }

    fun addTrainingDay(periodId: String, day: TrainingDay) {
        viewModelScope.launch {
            repository.addTrainingDay(userId, periodId, day)
            loadTrainingDays(periodId)
        }
    }

    fun addExercise(periodId: String, dayId: String, exercise: Exercise) {
        viewModelScope.launch {
            repository.addExercise(userId, periodId, dayId, exercise)
            loadExercises(periodId, dayId)
        }
    }
}
