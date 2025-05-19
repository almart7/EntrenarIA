package com.alessandra.entrenaria.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.data.model.Exercise

@Composable
fun RegisterRepsDialog(
    exercise: Exercise,
    initialNotes: String = exercise.notes,
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit
) {
    val context = LocalContext.current

    var repsInputs by remember { mutableStateOf(exercise.sets.map { it.actualReps?.toString() ?: "" }) }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar repeticiones") },
        text = {
            Column {
                Text(
                    // Nombre del ejercicio
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Por cada set, hay un campo para introducir las repeticiones realizadas
                exercise.sets.forEachIndexed { index, set ->
                    OutlinedTextField(
                        value = repsInputs.getOrElse(index) { "" },
                        // Guarda los valores introducidos en una lista
                        onValueChange = { newValue ->
                            repsInputs = repsInputs.toMutableList().also {
                                it[index] = newValue
                            }
                        },
                        // Se indican las repeticiones que se esperan para ese set
                        label = {
                            Text("Set ${index + 1} (objetivo: ${set.targetRepsMin}-${set.targetRepsMax})")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // campo para notas del ejercicio
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        // Confirmación del registro de repeticiones
        confirmButton = {
            TextButton(onClick = {
                // Actualiza las repeticiones registradas para cada set
                val updatedSets = exercise.sets.mapIndexed { i, set ->
                    val reps = repsInputs.getOrNull(i)?.toIntOrNull()
                    if (reps == null || reps < 0) {
                        Toast.makeText(
                            context,
                            "Corrige los sets: asegúrate de que las repeticiones sean números válidos",
                            Toast.LENGTH_LONG
                        ).show()
                        return@TextButton
                    }
                    set.copy(actualReps = reps)
                }

                // Actualiza el ejercicio con las repeticiones registradas
                val updatedExercise = exercise.copy(
                    sets = updatedSets,
                    notes = notes
                )
                onConfirm(updatedExercise)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
