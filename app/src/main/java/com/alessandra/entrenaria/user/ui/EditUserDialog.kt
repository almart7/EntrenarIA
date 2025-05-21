package com.alessandra.entrenaria.user.ui

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.user.data.repository.UserRepository
import com.alessandra.entrenaria.user.ViewModel.UserViewModel
import com.alessandra.entrenaria.user.ViewModel.UserViewModelFactory

@Composable
fun EditUserDialog(
    currentName: String,
    currentAge: Int?,
    currentGender: String?,
    onDismiss: () -> Unit,
    onUserUpdated: () -> Unit
) {
    val context = LocalContext.current

    // ViewModel inyectado con repositorio
    val repository = remember { UserRepository() }
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(repository)
    )

    var name by remember { mutableStateOf(currentName) }
    var age by remember { mutableStateOf(currentAge?.toString() ?: "") }
    val genderOptions = listOf("Hombre", "Mujer", "Otro")
    var selectedGender by remember { mutableStateOf(currentGender) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val ageValue = age.toIntOrNull()
                    if (ageValue != null && selectedGender != null) {
                        viewModel.updateUserProfile(
                            name = name,
                            age = ageValue,
                            gender = selectedGender!!,
                            onSuccess = {
                                onUserUpdated()
                                onDismiss()
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                            }
                        )
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
            Text("Editar Perfil", color = MaterialTheme.colorScheme.onSurface)
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
