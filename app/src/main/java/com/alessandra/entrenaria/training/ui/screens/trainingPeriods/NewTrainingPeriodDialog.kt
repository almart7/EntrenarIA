package com.alessandra.entrenaria.training.ui.screens.trainingPeriods

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.training.data.model.TrainingPeriod
import com.alessandra.entrenaria.core.utils.formatAsDate
import com.alessandra.entrenaria.core.utils.showDatePicker
import com.google.firebase.Timestamp

@Composable
fun NewTrainingPeriodDialog(
    userId: String,
    period: TrainingPeriod? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, start: Timestamp, end: Timestamp) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(period?.title ?: "") }
    var notes by remember { mutableStateOf(period?.notes ?: "") }
    var startDate by remember { mutableStateOf(period?.startDate) }
    var endDate by remember { mutableStateOf(period?.endDate) }

    // dialogo para añadir o editar un periodo (si edita carga los datos previos)
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text(if (period == null) "Nuevo Bloque" else "Editar Bloque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 50) notes = it }, // límite de 50 caracteres,
                    label = { Text("Notas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Texto clicable para seleccionar fecha de inicio
                Text(
                    text = "Inicio: ${startDate?.formatAsDate() ?: "Sin seleccionar"}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker(context, startDate) { selected ->
                                startDate = selected
                                if (endDate != null && endDate!! < selected) {
                                    endDate = null
                                }
                            }
                        },
                    color = MaterialTheme.colorScheme.primary
                )

                // Texto clicable para seleccionar fecha de fin
                Text(
                    text = "Fin: ${endDate?.formatAsDate() ?: "Sin seleccionar"}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = startDate != null) {
                            if (startDate != null) {
                                showDatePicker(
                                    context,
                                    endDate ?: startDate,
                                    minDate = startDate?.toDate()
                                ) { selected ->
                                    endDate = selected
                                }
                            }
                        },
                    color = if (startDate != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                if (startDate != null && endDate != null && endDate!! < startDate!!) {
                    Text(
                        "La fecha de fin no puede ser anterior a la de inicio.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            val infoValida = title.isNotBlank() && startDate != null && endDate != null && endDate!! >= startDate!!
            TextButton(
                onClick = {
                    onConfirm(title, notes, startDate!!, endDate!!)
                },
                enabled = infoValida
            ) {
                Text(if (period == null) "Crear" else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}
