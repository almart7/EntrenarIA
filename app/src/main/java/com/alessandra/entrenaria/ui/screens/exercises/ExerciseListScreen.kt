package com.alessandra.entrenaria.ui.screens.exercises

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
                                        // Aquí puedes abrir detalles o edición si lo deseas
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
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            exercise.weight?.let {
                                Text("Peso: ${it} kg")
                            }
                            Text("Sets: ${exercise.sets.size}")
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
    }
}
