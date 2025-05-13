package com.alessandra.entrenaria.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alessandra.entrenaria.navigation.NewExercise
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    userId: String,
    exerciseId: String,
    navController: NavController
) {
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(TrainingRepository(), userId)
    )
    val exercise by viewModel.exerciseDetail.collectAsState()

    var showRepsDialog by remember { mutableStateOf(false) }
    var repsInputs by remember { mutableStateOf<List<String>>(emptyList()) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetailById(exerciseId)
    }

    exercise?.let { ex ->
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(ex.name) })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ex.weight?.let { Text("Peso: $it kg") }
                Text("Sets: ${ex.sets.size}")

                if (!ex.instructions.isNullOrBlank()) {
                    Text("Instrucciones:", style = MaterialTheme.typography.labelMedium)
                    Text(ex.instructions)
                }

                if (!ex.notes.isNullOrBlank()) {
                    Text("Notas:", style = MaterialTheme.typography.labelMedium)
                    Text(ex.notes)
                }

                Text("Detalles por set:", style = MaterialTheme.typography.labelMedium)
                ex.sets.forEachIndexed { index, set ->
                    val actual = set.actualReps?.let { " – ${it} reps realizadas" } ?: ""
                    Text("Set ${index + 1}: ${set.targetRepsMin}-${set.targetRepsMax} reps$actual")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón: Editar
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            navController.navigate(NewExercise(ex.periodId, ex.dayId, ex.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Editar objetivos")
                    }
                }

                // Botón: Registrar o editar marcas
                val alreadyRegistered = ex.sets.any { it.actualReps != null }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            repsInputs = ex.sets.map { it.actualReps?.toString() ?: "" }
                            notes = ex.notes
                            showRepsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (alreadyRegistered) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (alreadyRegistered) "Editar marcas" else "Registrar marcas")
                    }
                }
            }
        }

        if (showRepsDialog) {
            AlertDialog(
                onDismissRequest = { showRepsDialog = false },
                title = { Text("Registrar repeticiones") },
                text = {
                    Column {
                        ex.sets.forEachIndexed { index, set ->
                            OutlinedTextField(
                                value = repsInputs.getOrElse(index) { "" },
                                onValueChange = { newValue ->
                                    repsInputs = repsInputs.toMutableList().also {
                                        it[index] = newValue
                                    }
                                },
                                label = {
                                    Text("Set ${index + 1} (objetivo: ${set.targetRepsMin}-${set.targetRepsMax})")
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val updatedSets = ex.sets.mapIndexed { i, set ->
                            val reps = repsInputs.getOrNull(i)?.toIntOrNull()
                            set.copy(actualReps = reps)
                        }
                        viewModel.updateExercise(
                            ex.copy(
                                sets = updatedSets,
                                notes = notes
                            )
                        )
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
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
