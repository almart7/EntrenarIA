package com.alessandra.entrenaria.ui.screens.trainingDays

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
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.navigation.TrainingDays
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.components.NewTrainingDayDialog
import com.alessandra.entrenaria.ui.components.handleBottomBarNavigation
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.alessandra.entrenaria.util.formatAsDate
import com.entrenaria.models.TrainingRepository

@Composable
fun TrainingDaysScreen(
    userId: String,
    periodId: String,
    onNavigateToExercises: (String, String) -> Unit,
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )

    val trainingDays by viewModel.trainingDays.collectAsState()
    val trainingPeriod by viewModel.trainingPeriod.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedDays = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dayToEdit by remember { mutableStateOf<TrainingDay?>(null) }

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
                currentDestination = TrainingDays(periodId),
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
            Text("Días de entrenamiento", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn {
                items(trainingDays.sortedByDescending { it.date }) { day ->
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
                                        onNavigateToExercises(periodId, day.id)
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
                                Text("Fecha: ${day.date.formatAsDate()}")
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
                    val trainingDay = TrainingDay(
                        id = dayToEdit?.id ?: "",
                        userId = userId,
                        periodId = periodId,
                        date = date,
                        label = label,
                        notes = notes
                    )
                    if (dayToEdit == null) {
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
                title = { Text("¿Eliminar días seleccionados?") },
                text = { Text("Se eliminarán todos los días seleccionados.") },
                onDismissRequest = {
                    showDeleteDialog = false
                    selectionMode = false
                    selectedDays.clear()
                },
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
