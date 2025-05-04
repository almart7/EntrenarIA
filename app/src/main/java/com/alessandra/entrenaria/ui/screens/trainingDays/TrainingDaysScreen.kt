package com.alessandra.entrenaria.ui.screens.trainingDays

import android.app.DatePickerDialog
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
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.navigation.ExerciseList
import com.alessandra.entrenaria.navigation.TrainingDays
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository
import com.google.firebase.Timestamp
import java.util.*

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
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedDays = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTrainingDays(periodId)
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
                FloatingActionButton(onClick = { showDialog = true }) {
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
                                        navController.navigate(ExerciseList(periodId = periodId, dayId = day.id))
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

                            if (selectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (it) selectedDays.add(day.id)
                                        else selectedDays.remove(day.id)
                                        if (selectedDays.isEmpty()) selectionMode = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            val calendar = Calendar.getInstance()
            var selectedDate by remember { mutableStateOf(calendar.time) }

            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    val newDay = TrainingDay(
                        userId = userId,
                        periodId = periodId,
                        date = Timestamp(selectedDate),
                        label = "Día de entrenamiento",
                        notes = ""
                    )
                    viewModel.addTrainingDay(newDay)
                    showDialog = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
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
