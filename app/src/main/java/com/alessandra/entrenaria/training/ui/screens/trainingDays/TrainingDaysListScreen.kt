package com.alessandra.entrenaria.training.ui.screens.trainingDays

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
import com.alessandra.entrenaria.training.data.model.TrainingDay
import com.alessandra.entrenaria.core.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.core.ui.commonComponents.ConfirmDeleteDialog
import com.alessandra.entrenaria.core.ui.commonComponents.handleBottomBarNavigation
import com.alessandra.entrenaria.training.ViewModel.TrainingViewModel
import com.alessandra.entrenaria.training.ViewModel.TrainingViewModelFactory
import com.alessandra.entrenaria.core.utils.formatAsDate
import com.alessandra.entrenaria.core.utils.isToday
import com.alessandra.entrenaria.training.data.repository.TrainingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDaysScreen(
    userId: String,
    periodId: String,
    onNavigateToExercises: (String, String) -> Unit,
    onNavigateToTrainings: () -> Unit,
    onNavigateToUser: () -> Unit,
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
        // obtiene los dias de entrenamiento correspondientes al periodo seleccionado
        viewModel.loadTrainingDays(periodId)
        // obtiene el periodo de entrenamiento (para otener las fechas permitidas para los días del periodo)
        viewModel.loadTrainingPeriodById(periodId)
    }

    Scaffold(
        // Barra superior con el título de la pantalla
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Días de Entrenamiento",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // botón para añadir o eliminar días de entrenamiento
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
                },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar día", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        // barra de navegación
        bottomBar = {
            BottomNavigationBar(
                currentScreenBottomBarItem = null,
                onNavigate = { destination ->
                    handleBottomBarNavigation(
                        destination = destination,
                        onTrainings = onNavigateToTrainings,
                        onUser = onNavigateToUser,
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
            // listado de dias de entrenamiento pertenecientes a un periodo ordenados por fecha
            LazyColumn {
                items(trainingDays.sortedBy { it.date }) { day ->
                    val isSelected = selectedDays.contains(day.id)
                    val isToday = isToday(day.date)

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
                            ),
                        // el color de la tarjeta cambia si contiene el día de hoy
                        colors = if (isToday)
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        else
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(day.label, style = MaterialTheme.typography.titleMedium)
                                // Mostrar las notas, si existen
                                if (day.notes.isNotBlank()) {
                                    Text(
                                        text = day.notes,
                                        //style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(day.date.formatAsDate(), style = MaterialTheme.typography.bodySmall)
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

        // dialogo para añadir o editar días
        if (showDialog && trainingPeriod != null) {
            val startDate = trainingPeriod!!.startDate.toDate()
            val endDate = trainingPeriod!!.endDate?.toDate()
            val defaultLabel = "Día ${trainingDays.size + 1}"

            NewTrainingDayDialog(
                userId = userId,
                periodId = periodId,
                day = dayToEdit,
                minDate = startDate,
                maxDate = endDate,
                defaultLabel = defaultLabel,
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
                        // añade dia
                        viewModel.addTrainingDay(trainingDay)
                    } else {
                        // actualiza dia
                        viewModel.updateTrainingDay(trainingDay)
                    }
                    showDialog = false
                    dayToEdit = null
                }
            )
        }

        // dialogo para eliminar días
        if (showDeleteDialog && selectedDays.isNotEmpty()) {
            ConfirmDeleteDialog(
                visible = true,
                title = "¿Eliminar días seleccionados?",
                text = "Esta acción eliminará todos los días seleccionados y sus datos.",
                onConfirm = {
                    // eliminación de dias y su conteido
                    selectedDays.forEach { viewModel.deleteTrainingDayWithChildren(it, periodId) }
                    Toast.makeText(context, "Días eliminados", Toast.LENGTH_SHORT).show()
                    showDeleteDialog = false
                    selectionMode = false
                    selectedDays.clear()
                },
                onDismiss = {
                    showDeleteDialog = false
                    selectionMode = false
                    selectedDays.clear()
                }
            )
        }
    }
}
