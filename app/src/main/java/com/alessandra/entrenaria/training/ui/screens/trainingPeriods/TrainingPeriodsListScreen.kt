package com.alessandra.entrenaria.training.ui.screens.trainingPeriods


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
import com.alessandra.entrenaria.training.data.model.TrainingPeriod
import com.alessandra.entrenaria.core.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.core.ui.commonComponents.handleBottomBarNavigation
import com.alessandra.entrenaria.training.ViewModel.TrainingViewModel
import com.alessandra.entrenaria.training.ViewModel.TrainingViewModelFactory
import com.alessandra.entrenaria.core.utils.formatAsDate
import com.alessandra.entrenaria.core.utils.isTodayInRange
import com.alessandra.entrenaria.training.data.repository.TrainingRepository
import com.alessandra.entrenaria.core.ui.commonComponents.BottomBarItem
import com.alessandra.entrenaria.core.ui.commonComponents.ConfirmDeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPeriodsScreen(
    userId: String,
    onNavigateToTrainingDays: (String) -> Unit,
    onNavigateToTrainings: () -> Unit,
    onNavigateToUser: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val repository = remember { TrainingRepository() }
    val viewModel: TrainingViewModel = viewModel(
        factory = TrainingViewModelFactory(repository, userId)
    )
    // collectAsState -> convierte un flow en un estado de compose (lista actualizada de periodos)
    val periods by viewModel.trainingPeriods.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedPeriods = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var periodToEdit by remember { mutableStateOf<TrainingPeriod?>(null) }

    // carga los periodos de entrenamiento del usuario (
    LaunchedEffect(Unit) {
        viewModel.loadTrainingPeriods()
    }

    Scaffold(
        // Barra superior con el título de la pantalla
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tus Bloques de Entrenamiento",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // botón para añadir o borrar periodos
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
                },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar periodo", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        // barra de navegación
        bottomBar = {
            BottomNavigationBar(
                currentScreenBottomBarItem = BottomBarItem.TrainingsItem,
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
            // listado de periodos de entrenamiento ordenado por fecha de inicio
            LazyColumn {
                items(periods.sortedByDescending { it.startDate }) { period ->
                    val isSelected = selectedPeriods.contains(period.id)
                    val containsToday = isTodayInRange(period.startDate,
                        period.endDate
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                // clik corto tiene varios comportamientos
                                onClick = {
                                    if (selectionMode) {
                                        if (isSelected) selectedPeriods.remove(period.id)
                                        else selectedPeriods.add(period.id)
                                        if (selectedPeriods.isEmpty()) selectionMode = false
                                    } else {
                                        onNavigateToTrainingDays(period.id)
                                    }
                                },
                                // click largo para seleccionar
                                onLongClick = {
                                    selectionMode = true
                                    if (!selectedPeriods.contains(period.id)) {
                                        selectedPeriods.add(period.id)
                                    }
                                }
                            ),
                        // el color de la tarjeta cambia si contiene el día de hoy
                        colors = if (containsToday)
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
                                Text(period.title, style = MaterialTheme.typography.titleMedium)
                                // Mostrar las notas, si existen
                                if (period.notes.isNotBlank()) {
                                    Text(
                                        text = period.notes,
                                       // style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text("Desde: ${period.startDate.formatAsDate()}", style = MaterialTheme.typography.bodySmall)
                                Text("Hasta: ${period.endDate?.formatAsDate()}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                // a la derecha puede haber un checkbox o un icono de edición
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

        // dialogo para añadir o editar un periodo
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
                    // Añade o actualiza un periodo
                    if (periodToEdit == null) viewModel.addTrainingPeriod(updated)
                    else viewModel.updateTrainingPeriod(updated)

                    showDialog = false
                    periodToEdit = null
                }
            )
        }

        // dialogo para confirmar la eliminación de periodos
        ConfirmDeleteDialog(
            visible = showDeleteDialog && selectedPeriods.isNotEmpty(),
            title = "¿Eliminar periodos seleccionados?",
            text = "Esta acción eliminará todos los periodos seleccionados y sus datos.",
            onConfirm = {
                // Elimina los periodos seleccionados
                selectedPeriods.forEach { viewModel.deleteTrainingPeriodWithChildren(it) }
                Toast.makeText(context, "Periodos eliminados", Toast.LENGTH_SHORT).show()
                showDeleteDialog = false
                selectionMode = false
                selectedPeriods.clear()
            },
            onDismiss = {
                showDeleteDialog = false
                selectionMode = false
                selectedPeriods.clear()
            }
        )


    }
}
