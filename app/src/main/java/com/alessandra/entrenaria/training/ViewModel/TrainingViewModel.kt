package com.alessandra.entrenaria.training.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alessandra.entrenaria.training.data.model.TrainingPeriod
import com.alessandra.entrenaria.training.data.model.TrainingDay
import com.alessandra.entrenaria.training.data.model.Exercise
import com.alessandra.entrenaria.training.data.repository.TrainingRepository
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

    // CARGA DE DATOS
    // Obtiene los periodos de entrenamiento del usuario actual
    fun loadTrainingPeriods() {
        viewModelScope.launch {
            _trainingPeriods.value = repository.getTrainingPeriods(userId)
        }
    }

    // Carga un periodo en concreto
    fun loadTrainingPeriodById(periodId: String) {
        viewModelScope.launch {
            _trainingPeriod.value = repository.getTrainingPeriodById(periodId)
        }
    }

    // Obtiene los dias de entrenamiento pertenecientes a un periodo
    fun loadTrainingDays(periodId: String) {
        viewModelScope.launch {
            _trainingDays.value = repository.getTrainingDays(userId, periodId)
        }
    }

    // Obtiene los ejercicios pertenecientes a un dia de entrenamiento
    fun loadExercises(dayId: String) {
        viewModelScope.launch {
            _exercises.value = repository.getExercises(userId, dayId)
        }
    }

    // Obtiene los nombres de los ejercicios del usuario actual
    fun fetchExerciseNames() {
        viewModelScope.launch {
            _exerciseNames.value = repository.getExerciseNamesForUser(userId)
        }
    }

    // obtiene un ejercicio en concreto para NewExerciseScreen (UI driven state)
    fun loadExerciseToEdit(id: String) {
        viewModelScope.launch {
            _exerciseToEdit.value = repository.getExerciseById(id)
        }
    }

    // Obtiene un ejercicio en concreto para DetailExerciseScreen (UI driven state)
    fun loadExerciseDetailById(id: String) {
        viewModelScope.launch {
            _exerciseDetail.value = repository.getExerciseById(id)
        }
    }

    // ADICIÓN DE DATOS (siempre hace una carga de datos al final para mantener la UI actualizada)
    // Periodo
    fun addTrainingPeriod(period: TrainingPeriod) {
        viewModelScope.launch {
            repository.addTrainingPeriod(period.copy(userId = userId))
            loadTrainingPeriods()
        }
    }
    // dia de entrenamiento a un periodo
    fun addTrainingDay(day: TrainingDay) {
        viewModelScope.launch {
            repository.addTrainingDay(day.copy(userId = userId))
            loadTrainingDays(day.periodId)
        }
    }

    // ejercicio a un dia de entrenamiento
    fun addExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.addExercise(exercise.copy(userId = userId))
            loadExercises(exercise.dayId)
        }
    }

    // BORRADO (siempre hace una carga de datos al final para mantener la UI actualizada)
    // Periodo y  su contenido (dias y ejercicios)
    fun deleteTrainingPeriodWithChildren(periodId: String) {
        viewModelScope.launch {
            repository.deleteTrainingPeriodWithChildren(userId, periodId)
            loadTrainingPeriods()
        }
    }

    // Dia y su contenido (ejercicios)
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

    // ACTUALIZACIÓN DE DATOS (siempre hace una carga de datos al final para mantener la UI actualizada)
    // Ejercicio
    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
            loadExercises(exercise.dayId)
        }
    }
    // Dia
    fun updateTrainingDay(day: TrainingDay) {
        viewModelScope.launch {
            repository.updateTrainingDay(day)
            loadTrainingDays(day.periodId)
        }
    }
    // Periodo
    fun updateTrainingPeriod(period: TrainingPeriod) {
        viewModelScope.launch {
            repository.updateTrainingPeriod(period)
            loadTrainingPeriods()
        }
    }

}
