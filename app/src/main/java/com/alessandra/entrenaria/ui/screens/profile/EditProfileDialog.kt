package com.alessandra.entrenaria.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAge: Int?,
    currentGender: String?,
    onDismiss: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf(currentName) }
    var age by remember { mutableStateOf(currentAge?.toString() ?: "") }
    val genderOptions = listOf("Hombre", "Mujer", "Otro")
    var selectedGender by remember { mutableStateOf(currentGender) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        val userData = hashMapOf<String, Any>(
                            "name" to name,
                            "age" to (age.toIntOrNull() ?: 0),
                            "gender" to (selectedGender ?: "")
                        )

                        db.collection("users").document(uid)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                onProfileUpdated()
                                onDismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = name.isNotBlank() && age.isNotBlank() && selectedGender != null
            ) {
                Text("Guardar", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        title = {
            Text(
                "Editar Perfil",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { newAge ->
                        if (newAge.all { it.isDigit() }) {
                            age = newAge
                        }
                    },
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { expanded = true }
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedGender ?: "Seleccionar Género",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genderOptions.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender) },
                            onClick = {
                                selectedGender = gender
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
}
