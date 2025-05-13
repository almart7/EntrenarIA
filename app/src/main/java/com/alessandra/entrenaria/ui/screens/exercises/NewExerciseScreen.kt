package com.alessandra.entrenaria.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.data.model.ExerciseSet
import com.alessandra.entrenaria.navigation.NewExercise
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

data class ExerciseSetUiModel(
    var targetRepsMin: String = "",
    var targetRepsMax: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExerciseScreen(
    userId: String,
    periodId: String,
    dayId: String,
    exerciseId: String? = null,
    navController: NavController
) {
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(TrainingRepository(), userId)
    )

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val sets = remember { mutableStateListOf(ExerciseSetUiModel()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exerciseNameSuggestions by viewModel.exerciseNames.collectAsState()
    val exerciseToEdit by viewModel.exerciseToEdit.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchExerciseNames()
        if (exerciseId != null) {
            viewModel.loadExerciseToEdit(exerciseId)
        }
    }

    LaunchedEffect(exerciseToEdit) {
        exerciseToEdit?.let { exercise ->
            name = exercise.name
            weight = exercise.weight?.toString() ?: ""
            notes = exercise.notes
            sets.clear()
            sets.addAll(
                exercise.sets.map {
                    ExerciseSetUiModel(
                        it.targetRepsMin.toString(),
                        it.targetRepsMax.toString()
                    )
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (exerciseId != null) "Editar ejercicio" else "Nuevo ejercicio")
            })
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = NewExercise(periodId, dayId, exerciseId)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        expanded = true
                    },
                    label = { Text("Nombre") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    exerciseNameSuggestions
                        .filter { it.contains(name, ignoreCase = true) && it != name }
                        .forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    name = suggestion
                                    expanded = false
                                }
                            )
                        }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() || ch == '.' }) weight = it
                },
                label = { Text("Peso (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instrucciones (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sets", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { sets.add(ExerciseSetUiModel()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir set")
                }
            }

            LazyColumn {
                itemsIndexed(sets) { index, set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = set.targetRepsMin,
                            onValueChange = {
                                sets[index] = set.copy(targetRepsMin = it.filter { ch -> ch.isDigit() })
                            },
                            label = { Text("Reps mín") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = set.targetRepsMax,
                            onValueChange = {
                                sets[index] = set.copy(targetRepsMax = it.filter { ch -> ch.isDigit() })
                            },
                            label = { Text("Reps máx") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        IconButton(onClick = {
                            val duplicatedSet = set.copy()
                            sets.add(index + 1, duplicatedSet)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar("Set duplicado", "Deshacer")
                                if (result == SnackbarResult.ActionPerformed) sets.removeAt(index + 1)
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Duplicar set")
                        }

                        IconButton(onClick = { sets.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar set")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalSets = sets.mapNotNull {
                        val min = it.targetRepsMin.toIntOrNull()
                        val max = it.targetRepsMax.toIntOrNull()
                        if (min != null && max != null) ExerciseSet(min, max) else null
                    }

                    val exercise = Exercise(
                        id = exerciseId ?: "",
                        name = name,
                        sets = finalSets,
                        weight = weight.toFloatOrNull(),
                        instructions = instructions,
                        userId = userId,
                        dayId = dayId,
                        periodId = periodId,
                        createdAt = Timestamp.now()
                    )

                    if (exerciseId != null) {
                        viewModel.updateExercise(exercise)
                    } else {
                        viewModel.addExercise(exercise)
                    }

                    navController.popBackStack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (exerciseId != null) "Guardar cambios" else "Guardar ejercicio")
            }
        }
    }
}
