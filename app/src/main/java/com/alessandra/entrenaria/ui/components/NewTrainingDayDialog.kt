package com.alessandra.entrenaria.ui.components

import android.app.DatePickerDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.alessandra.entrenaria.data.model.TrainingDay
import com.alessandra.entrenaria.util.formatAsDate
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
    var selectedDate by remember { mutableStateOf<Date?>(day?.date?.toDate()) }

    var label by remember { mutableStateOf(day?.label ?: "") }
    var notes by remember { mutableStateOf(day?.notes ?: "") }

    val context = LocalContext.current

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

                Text("Fecha: ${selectedDate?.formatAsDate() ?: "Sin seleccionar"}")

                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    selectedDate?.let { calendar.time = it }

                    val picker = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            selectedDate = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    if (minDate != null) picker.datePicker.minDate = minDate.time
                    if (maxDate != null) picker.datePicker.maxDate = maxDate.time

                    picker.show()
                }) {
                    Text("Seleccionar fecha")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedDate?.let {
                        onConfirm(label, notes, Timestamp(it))
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
