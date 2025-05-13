package com.alessandra.entrenaria.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.google.firebase.Timestamp
import java.util.*
import com.alessandra.entrenaria.util.formatAsDate

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

    var startDate by remember { mutableStateOf(period?.startDate?.toDate()) }
    var endDate by remember { mutableStateOf(period?.endDate?.toDate()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (period == null) "Nuevo periodo" else "Editar periodo") },
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
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Inicio: ${startDate?.formatAsDate() ?: "Sin seleccionar"}")
                Button(onClick = {
                    showDatePicker(context, startDate) { selected ->
                        startDate = selected
                        // Reset end date si ya no es válida
                        if (endDate != null && endDate!! < selected) {
                            endDate = null
                        }
                    }
                }) {
                    Text("Seleccionar fecha de inicio")
                }

                Text("Fin: ${endDate?.formatAsDate() ?: "Sin seleccionar"}")
                Button(onClick = {
                    showDatePicker(context, endDate ?: startDate ?: Date()) { selected ->
                        endDate = selected
                    }
                }, enabled = startDate != null) {
                    Text("Seleccionar fecha de fin")
                }

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
            val fechasValidas = title.isNotBlank() && startDate != null && endDate != null && endDate!! >= startDate!!
            TextButton(
                onClick = {
                    onConfirm(
                        title,
                        notes,
                        Timestamp(startDate!!),
                        Timestamp(endDate!!)
                    )
                },
                enabled = fechasValidas
            ) {
                Text(if (period == null) "Crear" else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun showDatePicker(context: android.content.Context, preselected: Date?, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    if (preselected != null) calendar.time = preselected

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}