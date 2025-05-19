package com.alessandra.entrenaria.ui.screens.trainingPeriods

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.alessandra.entrenaria.ui.components.NewTrainingPeriodDialog
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModel
import com.alessandra.entrenaria.ui.viewmodel.TrainingViewModelFactory
import com.entrenaria.models.TrainingRepository
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.navigation.TrainingPeriods
import com.alessandra.entrenaria.ui.components.handleBottomBarNavigation
import com.alessandra.entrenaria.util.formatAsDate



@Composable
fun TrainingPeriodsScreen(
    userId: String,
    onNavigateToTrainingDays: (String) -> Unit,
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )
    val periods by viewModel.trainingPeriods.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedPeriods = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var periodToEdit by remember { mutableStateOf<TrainingPeriod?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadTrainingPeriods()
    }

    Scaffold(
        floatingActionButton = {
            if (selectionMode && selectedPeriods.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onError)
                }
            } else {
                FloatingActionButton(onClick = {
                    periodToEdit = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar periodo")
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentDestination = TrainingPeriods,
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
            Text("Tus entrenamientos", style = MaterialTheme.typography.titleLarge)

            LazyColumn {
                items(periods) { period ->
                    val isSelected = selectedPeriods.contains(period.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) selectedPeriods.remove(period.id)
                                        else selectedPeriods.add(period.id)
                                        if (selectedPeriods.isEmpty()) selectionMode = false
                                    } else {
                                        onNavigateToTrainingDays(period.id)
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    if (!selectedPeriods.contains(period.id)) {
                                        selectedPeriods.add(period.id)
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
                                Text(period.title, style = MaterialTheme.typography.titleMedium)
                                Text("Desde: ${period.startDate.formatAsDate()}")
                                period.endDate?.let { Text("Hasta: ${it.formatAsDate()}") }
                            }
                            Row {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedPeriods.add(period.id)
                                            else selectedPeriods.remove(period.id)
                                            if (selectedPeriods.isEmpty()) selectionMode = false
                                        }
                                    )
                                } else {
                                    IconButton(onClick = {
                                        periodToEdit = period
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar periodo")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            NewTrainingPeriodDialog(
                userId = userId,
                period = periodToEdit,
                onDismiss = {
                    showDialog = false
                    periodToEdit = null
                },
                onConfirm = { title, notes, start, end ->
                    val updated = TrainingPeriod(
                        id = periodToEdit?.id ?: "",
                        userId = userId,
                        title = title,
                        notes = notes,
                        startDate = start,
                        endDate = end
                    )
                    if (periodToEdit == null) viewModel.addTrainingPeriod(updated)
                    else viewModel.updateTrainingPeriod(updated)

                    showDialog = false
                    periodToEdit = null
                }
            )
        }

        if (showDeleteDialog && selectedPeriods.isNotEmpty()) {
            AlertDialog(
                title = { Text("¿Eliminar periodos seleccionados?") },
                text = { Text("Esta acción eliminará todos los periodos seleccionados y sus datos.") },
                onDismissRequest = {
                    showDeleteDialog = false
                    selectionMode = false
                    selectedPeriods.clear()
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedPeriods.forEach { viewModel.deleteTrainingPeriodWithChildren(it) }
                        Toast.makeText(context, "Periodos eliminados", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                        selectionMode = false
                        selectedPeriods.clear()
                    }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        selectionMode = false
                        selectedPeriods.clear()
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
