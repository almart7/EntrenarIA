package com.alessandra.entrenaria.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.data.model.TrainingPeriod
import com.google.firebase.Timestamp
import com.alessandra.entrenaria.util.formatAsDate
import com.alessandra.entrenaria.util.showDatePicker

@Composable
fun NewTrainingPeriodDialog(
    userId: String,
    period: TrainingPeriod? = null,  // Null si se está creando un nuevo periodo
    onDismiss: () -> Unit, // cierra el diálogo
    onConfirm: (title: String, notes: String, start: Timestamp, end: Timestamp) -> Unit // Envia los datos al ViewModel
) {
    val context = LocalContext.current
    // Rellena los campos con los datos del periodo si se está editando
    // Remember para mantener los valores actualizados
    var title by remember { mutableStateOf(period?.title ?: "") }
    var notes by remember { mutableStateOf(period?.notes ?: "") }
    var startDate by remember { mutableStateOf(period?.startDate) }
    var endDate by remember { mutableStateOf(period?.endDate) }

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
                        if (endDate != null && endDate!! < selected) {
                            endDate = null
                        }
                    }
                }) {
                    Text("Seleccionar fecha de inicio")
                }

                Text("Fin: ${endDate?.formatAsDate() ?: "Sin seleccionar"}")
                // Solo puedes elegir end date si ya has puesto start date
                Button(onClick = {
                    showDatePicker(context, endDate ?: startDate, minDate = startDate?.toDate()) { selected ->
                        endDate = selected
                    }
                }, enabled = startDate != null) {
                    Text("Seleccionar fecha de fin")
                }

                // Error si la fecha de fin es anterior a la de inicio
                if (startDate != null && endDate != null && endDate!! < startDate!!) {
                    Text(
                        "La fecha de fin no puede ser anterior a la de inicio.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        // solo puedes guardar si se han rellenado los campos correctamente
        confirmButton = {
            val infoValida = title.isNotBlank() && startDate != null && endDate != null && endDate!! >= startDate!!
            TextButton(
                onClick = {
                    // Envia datos a viewmodel
                    onConfirm(
                        title,
                        notes,
                        startDate!!,
                        endDate!!
                    )
                },
                enabled = infoValida
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

