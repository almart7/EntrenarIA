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
    navController: NavController
) {
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(TrainingRepository(), userId)
    )

    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val sets = remember { mutableStateListOf(ExerciseSetUiModel()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nuevo ejercicio") })
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = NewExercise(periodId, dayId)
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sets", style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { sets.add(ExerciseSetUiModel()) },
                    modifier = Modifier.size(36.dp)
                ) {
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
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = set.targetRepsMax,
                            onValueChange = {
                                sets[index] = set.copy(targetRepsMax = it.filter { ch -> ch.isDigit() })
                            },
                            label = { Text("Reps máx") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )

                        IconButton(onClick = {
                            val duplicatedSet = set.copy()
                            sets.add(index + 1, duplicatedSet)

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Set duplicado",
                                    actionLabel = "Deshacer"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    sets.removeAt(index + 1)
                                }
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
                        name = name,
                        sets = finalSets,
                        weight = weight.toFloatOrNull(),
                        notes = notes,
                        userId = userId,
                        dayId = dayId,
                        periodId = periodId
                    )

                    viewModel.addExercise(exercise)
                    navController.popBackStack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar ejercicio")
            }
        }
    }
}

