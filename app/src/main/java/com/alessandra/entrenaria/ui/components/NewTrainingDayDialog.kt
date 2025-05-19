package com.alessandra.entrenaria.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.util.formatAsDate
import com.alessandra.entrenaria.util.showDatePicker
import com.google.firebase.Timestamp
import java.util.*

@Composable
fun NewTrainingDayDialog(
    userId: String,
    periodId: String,
    day: TrainingDay? = null,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDismiss: () -> Unit,
    onConfirm: (label: String, notes: String, date: Timestamp) -> Unit
) {
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf(day?.date) }
    var label by remember { mutableStateOf(day?.label ?: "") }
    var notes by remember { mutableStateOf(day?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (day == null) "Nuevo día" else "Editar día") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Fecha: ${selectedDate?.toDate()?.formatAsDate() ?: "Sin seleccionar"}")

                Button(onClick = {
                    showDatePicker(
                        context = context,
                        preselected = selectedDate,
                        minDate = minDate,
                        maxDate = maxDate
                    ) { selected ->
                        selectedDate = selected
                    }
                }) {
                    Text("Seleccionar fecha")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedDate?.let {
                        onConfirm(label, notes, it)
                    }
                },
                enabled = label.isNotBlank() && selectedDate != null
            ) {
                Text(if (day == null) "Crear" else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
