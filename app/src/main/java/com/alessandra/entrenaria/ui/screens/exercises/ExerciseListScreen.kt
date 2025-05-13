package com.alessandra.entrenaria.ui.screens.exercises

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.navigation.ExerciseDetail
import com.alessandra.entrenaria.navigation.ExerciseList
import com.alessandra.entrenaria.navigation.NewExercise
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository

@Composable
fun ExerciseListScreen(
    userId: String,
    periodId: String,
    dayId: String,
    navController: NavController
) {
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )

    val exercises by viewModel.exercises.collectAsState()
    val context = LocalContext.current

    var selectionMode by remember { mutableStateOf(false) }
    val selectedExercises = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showRepsDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var actualRepsInputs by remember { mutableStateOf<List<String>>(emptyList()) }
    var exerciseNotes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadExercises(dayId)
    }

    Scaffold(
        floatingActionButton = {
            if (selectionMode && selectedExercises.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onError)
                }
            } else {
                FloatingActionButton(onClick = {
                    navController.navigate(NewExercise(periodId, dayId))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio")
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = ExerciseList(periodId, dayId)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Ejercicios del día",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(exercises) { exercise ->
                    val isSelected = selectedExercises.contains(exercise.id)
                    val hasActualReps = exercise.sets.any { it.actualReps != null }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) selectedExercises.remove(exercise.id)
                                        else selectedExercises.add(exercise.id)
                                        if (selectedExercises.isEmpty()) selectionMode = false
                                    } else {
                                        // Navegación a detalle
                                         navController.navigate(ExerciseDetail(exercise.id))
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    if (!selectedExercises.contains(exercise.id)) {
                                        selectedExercises.add(exercise.id)
                                    }
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                                exercise.weight?.let {
                                    Text("Peso: ${it} kg")
                                }
                                Text("Sets: ${exercise.sets.size}")
                            }

                            Row {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedExercises.add(exercise.id)
                                            else selectedExercises.remove(exercise.id)
                                            if (selectedExercises.isEmpty()) selectionMode = false
                                        }
                                    )
                                } else {
                                    IconButton(onClick = {
                                        selectedExercise = exercise
                                        actualRepsInputs = exercise.sets.map { it.actualReps?.toString() ?: "" }
                                        exerciseNotes = exercise.notes
                                        showRepsDialog = true
                                    }) {
                                        Icon(
                                            imageVector = if (hasActualReps) Icons.Default.Check else Icons.Default.PlayArrow,
                                            contentDescription = if (hasActualReps) "Completado" else "Registrar repeticiones",
                                            tint = if (hasActualReps) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && selectedExercises.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    selectionMode = false
                    selectedExercises.clear()
                },
                title = { Text("¿Eliminar ejercicios seleccionados?") },
                text = { Text("Se eliminarán todos los ejercicios seleccionados.") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedExercises.forEach {
                            viewModel.deleteExercise(it)
                        }
                        Toast.makeText(context, "Ejercicios eliminados", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                        selectionMode = false
                        selectedExercises.clear()
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        selectionMode = false
                        selectedExercises.clear()
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showRepsDialog && selectedExercise != null) {
            AlertDialog(
                onDismissRequest = { showRepsDialog = false },
                title = { Text("Repeticiones realizadas") },
                text = {
                    Column {
                        Text(
                            text = selectedExercise!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        selectedExercise!!.sets.forEachIndexed { index, set ->
                            OutlinedTextField(
                                value = actualRepsInputs.getOrElse(index) { "" },
                                onValueChange = { newValue ->
                                    actualRepsInputs = actualRepsInputs.toMutableList().also {
                                        it[index] = newValue
                                    }
                                },
                                label = {
                                    Text("Set ${index + 1} (objetivo: ${set.targetRepsMin}-${set.targetRepsMax})")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = exerciseNotes,
                            onValueChange = { exerciseNotes = it },
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val updatedSets = selectedExercise!!.sets.mapIndexed { index, set ->
                            val reps = actualRepsInputs.getOrNull(index)?.toIntOrNull()
                            set.copy(actualReps = reps)
                        }
                        val updatedExercise = selectedExercise!!.copy(
                            sets = updatedSets,
                            notes = exerciseNotes
                        )
                        viewModel.updateExercise(updatedExercise)
                        showRepsDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRepsDialog = false
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

