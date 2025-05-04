package com.alessandra.entrenaria.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alessandra.entrenaria.navigation.Profile
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.components.EditProfileDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf<String>("") }
    var name by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // ✅ Aquí defines la función
    fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            val uid = user.uid
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        email = document.getString("email") ?: ""
                        name = document.getString("name")
                        gender = document.getString("gender")
                        age = document.getLong("age")?.toInt()
                    }
                }
        }
    }

    // Se ejecuta al entrar a la pantalla
    LaunchedEffect(Unit) {
        loadUserData()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentDestination = Profile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Perfil",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text("Email: $email", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text("Nombre: ${name ?: "No definido"}", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text("Género: ${gender ?: "No definido"}", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text("Edad: ${age?.toString() ?: "No definido"}", fontSize = 20.sp, modifier = Modifier.padding(bottom = 32.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Editar Perfil")
            }

            Button(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                currentName = name ?: "",
                currentAge = age,
                currentGender = gender,
                onDismiss = { showEditDialog = false },
                onProfileUpdated = {
                    showEditDialog = false
                    loadUserData() // Refresca los datos del perfil tras actualizarlos
                }
            )

        }
    }
}
