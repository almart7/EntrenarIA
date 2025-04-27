package com.alessandra.entrenaria.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProfileScreen(
    onProfileUpdated: () -> Unit = {},
    onCancel: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var genderOptions = listOf("Hombre", "Mujer", "Otro")
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Editar Perfil",
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
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

        TextField(
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

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = selectedGender ?: "Seleccionar Género",
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
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

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    val updates = hashMapOf<String, Any?>(
                        "name" to name.ifBlank { null },
                        "age" to age.toIntOrNull(),
                        "gender" to selectedGender
                    )
                    db.collection("users").document(uid)
                        .update(updates)
                        .addOnSuccessListener {
                            onProfileUpdated()
                        }
                        .addOnFailureListener { e ->
                            // Puedes mostrar error aquí si quieres
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar Cambios")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onCancel() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text(text = "Cancelar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}
