package com.alessandra.entrenaria.training.ui.screens.exercises

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.training.data.model.Exercise

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

    // Asegura que al cambiar de ejercicio se carguen sus notas previas correctamente
    LaunchedEffect(exercise.id) {
        repsInputs = exercise.sets.map { it.actualReps?.toString() ?: "" }
        notes = exercise.notes
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text("Registrar repeticiones") },
        text = {
            Column {
                // Nombre del ejercicio
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Campo para cada set
                exercise.sets.forEachIndexed { index, set ->
                    OutlinedTextField(
                        value = repsInputs.getOrElse(index) { "" },
                        onValueChange = { newValue ->
                            repsInputs = repsInputs.toMutableList().also {
                                it[index] = newValue
                            }
                        },
                        label = {
                            Text("Set ${index + 1} (objetivo: ${set.targetRepsMin}-${set.targetRepsMax})")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 100) notes = it }, // max 100 caracteres
                    label = { Text("Notas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },

        // Confirmar
        confirmButton = {
            TextButton(onClick = {
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

                val updatedExercise = exercise.copy(
                    sets = updatedSets,
                    notes = notes
                )
                onConfirm(updatedExercise)
            }) {
                Text("Guardar")
            }
        },

        // Cancelar con color distintivo
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
