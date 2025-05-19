package com.alessandra.entrenaria.ui.screens.exercises

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.data.model.Exercise
import com.alessandra.entrenaria.navigation.ExerciseList
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.components.RegisterRepsDialog
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository
import com.alessandra.entrenaria.ui.components.handleBottomBarNavigation

@Composable
fun ExerciseListScreen(
    userId: String,
    periodId: String,
    dayId: String,
    onNavigateToExerciseDetail: (String) -> Unit,
    onNavigateToNewExercise: () -> Unit,
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val repository = remember { TrainingRepository() }
    // Factory para inyectar el userId y repositorio al ViewModel
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )

    // Observa el stateFlow para actualizar la lista de ejercicios si hay cambios
    val exercises by viewModel.exercises.collectAsState()
    val context = LocalContext.current

    // Estados para controlar la vista
    var selectionMode by remember { mutableStateOf(false) } //  Modo selección múltiple
    val selectedExercises = remember { mutableStateListOf<String>() } // Lista de ejercicios seleccionados
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) } // Ejercicio seleccionado para registrar repeticiones
    var showDeleteDialog by remember { mutableStateOf(false) } // Diálogo de eliminar
    var showRepsDialog by remember { mutableStateOf(false) } // Diálogo para registrar repeticiones

    // carga de datos inicial (ejercicios del dia escogido)
    LaunchedEffect(Unit) {
        viewModel.loadExercises(dayId)
    }

    // UI
    Scaffold(
        // Botón flotante cambiante según el modo de selección
        floatingActionButton = {
            // Si hay ejercicios selecccionados -> borrar
            if (selectionMode && selectedExercises.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onError)
                }
            // Si no hay ejercicios seleccionados -> añadir nuevo (navega a pantalla de añadir ejercicio)
            } else {
                FloatingActionButton(onClick = {
                    //navController.navigate(NewExercise(periodId, dayId)
                    onNavigateToNewExercise()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio")
                }
            }
        },
        // Menú inferior
        bottomBar = {
            BottomNavigationBar(
                currentDestination = ExerciseList(periodId, dayId),
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
            Text(
                text = "Ejercicios del día",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Lista de ejercicios
            LazyColumn {
                items(exercises) { exercise ->
                    val isSelected = selectedExercises.contains(exercise.id)
                    val hasActualReps = exercise.sets.any { it.actualReps != null }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                // Un click corto puede tener 2 comportamientos
                                onClick = {
                                    // Si estamos en modo selección
                                    if (selectionMode) {
                                        // Si el ejercicio ya está seleccionado, lo quita
                                        if (isSelected) selectedExercises.remove(exercise.id)
                                        // si no, lo añade a la selección
                                        else selectedExercises.add(exercise.id)
                                        // Si después de quitar un ejercicio no queda ninguno seleccionado, se desactiva el modo selección
                                        if (selectedExercises.isEmpty()) selectionMode = false
                                    } else {
                                        // Si no estamos en modo selección, navega a la pantalla de detalles del ejercicio (permite editar objetivos del ejercicio)
                                        onNavigateToExerciseDetail(exercise.id)
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    // Añade el ejercicio seleccionado a la lista
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
                            // Información del ejercicio
                            Column(Modifier.weight(1f)) {
                                Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                                exercise.weight?.let {
                                    Text("Peso: ${it} kg")
                                }
                                Text("Sets: ${exercise.sets.size}")
                            }

                            Row {
                                if (selectionMode) {
                                    // Checkbox de selección (si estamos en modo selección)
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedExercises.add(exercise.id)
                                            else selectedExercises.remove(exercise.id)
                                            if (selectedExercises.isEmpty()) selectionMode = false
                                        }
                                    )
                                } else {
                                    // Registro de repeticiones realizadas (abre un diálogo)
                                    IconButton(onClick = {
                                        selectedExercise = exercise
                                        showRepsDialog = true
                                    }) {
                                        // El icono cambia dependiendo de si ya ha repeticiones registradas
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

        // Diálogo para eliminar ejercicios
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

        // Diálogo para registrar repeticiones realizadas de un ejercicio
        if (showRepsDialog && selectedExercise != null) {
            RegisterRepsDialog(
                exercise = selectedExercise!!,
                onDismiss = { showRepsDialog = false },
                onConfirm = { updated ->
                    // Guarda el ejercicio actualizado en firestore
                    viewModel.updateExercise(updated)
                    showRepsDialog = false
                }
            )
        }
    }
}