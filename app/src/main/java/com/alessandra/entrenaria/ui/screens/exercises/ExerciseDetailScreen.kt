package com.alessandra.entrenaria.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.navigation.ExerciseDetail
import com.alessandra.entrenaria.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.ui.commonComponents.handleBottomBarNavigation
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.alessandra.entrenaria.model.TrainingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    userId: String,
    exerciseId: String,
    onNavigateToEditExercise: (String, String, String) -> Unit, // periodId, dayId, exerciseId
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    // Factory para inyectar el userId y repositorio al ViewModel
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(TrainingRepository(), userId)
    )

    // Observa los datos del ejercicio
    val exercise by viewModel.exerciseDetail.collectAsState()

    // Controla si se debe mostrar el diálogo para registrar repeticiones
    var showRepsDialog by remember { mutableStateOf(false) }

    // carga de datos inicial (detalles del ejercicio)
    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetailById(exerciseId)
    }

    // Si el ejercicio se ha cargado correctamente
    exercise?.let { ex ->
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(ex.name.ifBlank { "Detalle del ejercicio" }) })
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
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // peso, si hay
                ex.weight?.let {
                    Text("Peso:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text ("$it kg")
                }

                // Instrucciones del ejercicio (si las hay)
                if (!ex.instructions.isNullOrBlank()) {
                    Text("Instrucciones:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(ex.instructions)
                }

                // Notas del usuario (si las hay)
                if (!ex.notes.isNullOrBlank()) {
                    Text("Notas:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(ex.notes)
                }

                // Mostrar los sets en formato limpio y claro
                Text("Sets realizados:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                ex.sets.forEachIndexed { index, set ->
                    val actual = set.actualReps?.toString() ?: "–"
                    Text(
                        text = "Set ${index + 1}: $actual reps",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "(objetivo: ${set.targetRepsMin}-${set.targetRepsMax})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para editar los objetivos (navega a pantalla de edición)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            onNavigateToEditExercise(ex.periodId, ex.dayId, ex.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar objetivos")
                        Spacer(Modifier.width(8.dp))
                        Text("Editar objetivos")
                    }
                }

                // Botón para registrar o editar repeticiones realizadas
                val alreadyRegistered = ex.sets.any { it.actualReps != null }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showRepsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (alreadyRegistered) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = "Registrar repeticiones"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (alreadyRegistered) "Editar marcas" else "Registrar marcas")
                    }
                }
            }
        }

        // Diálogo para registrar repeticiones realizadas de un ejercicio
        if (showRepsDialog) {
            RegisterRepsDialog(
                exercise = ex,
                onDismiss = { showRepsDialog = false },
                onConfirm = { updated ->
                    // Guarda el ejercicio actualizado en firestore
                    viewModel.updateExercise(updated)
                    showRepsDialog = false
                }
            )
        }
    } ?: run {
        // Si los datos aún se están cargando, muestra un indicador de carga
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
