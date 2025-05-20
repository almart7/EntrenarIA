package com.alessandra.entrenaria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.model.TrainingRepository
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

    private val _trainingPeriod = MutableStateFlow<TrainingPeriod?>(null)
    val trainingPeriod: StateFlow<TrainingPeriod?> = _trainingPeriod

    private val _exerciseToEdit = MutableStateFlow<Exercise?>(null)
    val exerciseToEdit: StateFlow<Exercise?> = _exerciseToEdit

    private val _exerciseDetail = MutableStateFlow<Exercise?>(null)
    val exerciseDetail: StateFlow<Exercise?> = _exerciseDetail

    private val _exerciseNames = MutableStateFlow<List<String>>(emptyList())
    val exerciseNames: StateFlow<List<String>> = _exerciseNames

    fun loadTrainingPeriods() {
        viewModelScope.launch {
            _trainingPeriods.value = repository.getTrainingPeriods(userId)
        }
    }

    fun loadTrainingPeriodById(periodId: String) {
        viewModelScope.launch {
            _trainingPeriod.value = repository.getTrainingPeriodById(periodId)
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

    fun deleteTrainingDayWithChildren(dayId: String, periodId: String) {
        viewModelScope.launch {
            repository.deleteTrainingDayWithChildren(userId, dayId)
            loadTrainingDays(periodId)
        }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(exerciseId)
                _exercises.value = _exercises.value.filterNot { it.id == exerciseId }
            } catch (_: Exception) {}
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
            loadExercises(exercise.dayId)
        }
    }

    fun updateTrainingDay(day: TrainingDay) {
        viewModelScope.launch {
            repository.updateTrainingDay(day)
            loadTrainingDays(day.periodId)
        }
    }

    fun updateTrainingPeriod(period: TrainingPeriod) {
        viewModelScope.launch {
            repository.updateTrainingPeriod(period)
            loadTrainingPeriods()
        }
    }

    fun fetchExerciseNames() {
        viewModelScope.launch {
            _exerciseNames.value = repository.getExerciseNamesForUser(userId)
        }
    }

    // para NewExerciseScreen
    fun loadExerciseToEdit(id: String) {
        viewModelScope.launch {
            _exerciseToEdit.value = repository.getExerciseById(id)
        }
    }

    // para DetailExerciseScreen
    fun loadExerciseDetailById(id: String) {
        viewModelScope.launch {
            _exerciseDetail.value = repository.getExerciseById(id)
        }
    }

}
