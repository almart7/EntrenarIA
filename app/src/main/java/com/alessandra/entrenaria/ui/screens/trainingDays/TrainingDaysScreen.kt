package com.alessandra.entrenaria.ui.screens.trainingDays

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.navigation.ExerciseList
import com.alessandra.entrenaria.navigation.TrainingDays
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.components.NewTrainingDayDialog
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository

@Composable
fun TrainingDaysScreen(
    userId: String,
    periodId: String,
    navController: NavController,
    onTrainingDayClick: (String) -> Unit = {}
) {
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )

    val trainingDays by viewModel.trainingDays.collectAsState()
    val trainingPeriod by viewModel.trainingPeriod.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var dayToEdit by remember { mutableStateOf<TrainingDay?>(null) }

    var selectionMode by remember { mutableStateOf(false) }
    val selectedDays = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTrainingDays(periodId)
        viewModel.loadTrainingPeriodById(periodId)
    }

    Scaffold(
        floatingActionButton = {
            if (selectionMode && selectedDays.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onError)
                }
            } else {
                FloatingActionButton(onClick = {
                    dayToEdit = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar día")
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = TrainingDays(periodId)
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
                text = "Días de entrenamiento",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(trainingDays) { day ->
                    val isSelected = selectedDays.contains(day.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) selectedDays.remove(day.id)
                                        else selectedDays.add(day.id)
                                        if (selectedDays.isEmpty()) selectionMode = false
                                    } else {
                                        // 👇 LOGS para depuración
                                        Log.d("TrainingNav", "⏩ Intentando navegar a ExerciseList")
                                        Log.d("TrainingNav", "🟢 periodId: $periodId")
                                        Log.d("TrainingNav", "🟢 dayId: ${day.id}")

                                        if (day.id.isNotBlank()) {
                                            navController.navigate(ExerciseList(periodId, day.id))
                                        } else {
                                            Log.w("TrainingNav", "⚠️ ID del día está en blanco. Navegación cancelada.")
                                            Toast.makeText(context, "Este día aún no está listo para navegar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    if (!selectedDays.contains(day.id)) {
                                        selectedDays.add(day.id)
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
                                Text(day.label, style = MaterialTheme.typography.titleMedium)
                                Text("Fecha: ${day.date.toDate()}")
                            }

                            Row {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedDays.add(day.id)
                                            else selectedDays.remove(day.id)
                                            if (selectedDays.isEmpty()) selectionMode = false
                                        }
                                    )
                                } else {
                                    IconButton(onClick = {
                                        dayToEdit = day
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar día")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog && trainingPeriod != null) {
            val startDate = trainingPeriod!!.startDate.toDate()
            val endDate = trainingPeriod!!.endDate?.toDate()

            NewTrainingDayDialog(
                userId = userId,
                periodId = periodId,
                day = dayToEdit,
                minDate = startDate,
                maxDate = endDate,
                onDismiss = {
                    showDialog = false
                    dayToEdit = null
                },
                onConfirm = { label, notes, date ->
                    val updated = dayToEdit
                    val trainingDay = TrainingDay(
                        id = updated?.id ?: "",
                        userId = userId,
                        periodId = periodId,
                        date = date,
                        label = label,
                        notes = notes
                    )
                    if (updated == null) {
                        viewModel.addTrainingDay(trainingDay)
                    } else {
                        viewModel.updateTrainingDay(trainingDay)
                    }
                    showDialog = false
                    dayToEdit = null
                }
            )
        }

        if (showDeleteDialog && selectedDays.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    selectionMode = false
                    selectedDays.clear()
                },
                title = { Text("¿Eliminar días seleccionados?") },
                text = { Text("Se eliminarán todos los días seleccionados.") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedDays.forEach {
                            viewModel.deleteTrainingDay(it)
                        }
                        Toast.makeText(context, "Días eliminados", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                        selectionMode = false
                        selectedDays.clear()
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        selectionMode = false
                        selectedDays.clear()
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}