package com.alessandra.entrenaria.ui.screens.exercises

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.data.model.ExerciseSet
import com.alessandra.entrenaria.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.alessandra.entrenaria.model.TrainingRepository
import com.google.firebase.Timestamp
import com.alessandra.entrenaria.ui.commonComponents.handleBottomBarNavigation

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
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )

    // Estados para campos del formulario
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val sets = remember { mutableStateListOf(ExerciseSetUiModel()) }

    val exerciseNameSuggestions by viewModel.exerciseNames.collectAsState()
    val exerciseToEdit by viewModel.exerciseToEdit.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // Carga de nombres y, si aplica, el ejercicio a editar
    LaunchedEffect(Unit) {
        viewModel.fetchExerciseNames()
        if (exerciseId != null) {
            viewModel.loadExerciseToEdit(exerciseId)
        }
    }

    // Rellena los campos si es edición
    LaunchedEffect(exerciseToEdit) {
        exerciseToEdit?.let { exercise ->
            name = exercise.name
            weight = exercise.weight?.toString() ?: ""
            instructions = exercise.instructions
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
                currentScreenBottomBarItem = null,
                onNavigate = { destination ->
                    handleBottomBarNavigation(
                        destination = destination,
                        onTrainings = onNavigateToTrainings,
                        onProfile = onNavigateToProfile,
                        onChat = onNavigateToChat
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Nombre
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

            // Peso
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

            // Instrucciones
            OutlinedTextField(
                value = instructions,
                onValueChange = { if (it.length <= 100) instructions = it },  // max 100 caracteres
                label = { Text("Instrucciones (opcional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Título y botón de añadir set
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

            // Lista de sets
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
                            val duplicated = set.copy()
                            sets.add(index + 1, duplicated)
                            Toast.makeText(context, "Set duplicado", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar set")
                        }

                        IconButton(onClick = { sets.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar set")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón de cancelar
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancelar")
            }

            // Botón de guardar
            Button(
                onClick = {
                    val parsedSets = sets.map { uiSet ->
                        val min = uiSet.targetRepsMin.toIntOrNull()
                        val max = uiSet.targetRepsMax.toIntOrNull()
                        if (min != null && max != null && min <= max) {
                            ExerciseSet(min, max)
                        } else null
                    }

                    if (parsedSets.any { it == null }) {
                        Toast.makeText(
                            context,
                            "Corrige los sets: asegúrate de que mín y máx sean números válidos y mín ≤ máx",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    val finalSets = parsedSets.filterNotNull()

                    val exercise = Exercise(
                        id = exerciseId ?: "",
                        name = name,
                        sets = finalSets,
                        weight = weight.toFloatOrNull(),
                        instructions = instructions,
                        notes = notes,
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
                    // Volver atrás tras guardar (a la lista de ejercicios)
                    onBack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (exerciseId != null) "Guardar cambios" else "Guardar ejercicio")
            }
        }
    }
}
