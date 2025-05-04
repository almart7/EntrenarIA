package com.alessandra.entrenaria.ui.components

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewTrainingPeriodDialog(
    userId: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, startDate: Timestamp, endDate: Timestamp) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf(Date()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            notes,
                            Timestamp(startDate),
                            Timestamp(endDate)
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Crear")
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Nuevo periodo de entrenamiento") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    maxLines = 3
                )

                Spacer(Modifier.height(16.dp))

                Text(text = "Inicio: ${formatter.format(startDate)}")
                Button(onClick = {
                    showDatePicker(context, startDate) { selected ->
                        startDate = selected
                    }
                }) {
                    Text("Seleccionar fecha de inicio")
                }

                Spacer(Modifier.height(8.dp))

                Text(text = "Fin: ${formatter.format(endDate)}")
                Button(onClick = {
                    showDatePicker(context, endDate) { selected ->
                        endDate = selected
                    }
                }) {
                    Text("Seleccionar fecha de fin")
                }
            }
        }
    )
}

// Función reutilizable para seleccionar una fecha
fun showDatePicker(
    context: Context,
    initialDate: Date = Date(),
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply { time = initialDate }

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
