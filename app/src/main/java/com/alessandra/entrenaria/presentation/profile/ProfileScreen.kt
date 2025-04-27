package com.alessandra.entrenaria.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Variables que vamos a mostrar
    var email by remember { mutableStateOf<String>("") }
    var name by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }

    // 🔥 Cargamos los datos de Firestore
    LaunchedEffect(Unit) {
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
                .addOnFailureListener {
                    // Opcional: puedes mostrar un error si falla la lectura
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil",
            color = Color.White,
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Email: $email",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Nombre: ${name ?: "No definido"}",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Género: ${gender ?: "No definido"}",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Edad: ${age?.toString() ?: "No definido"}",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onEditProfile() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(text = "Editar Perfil")
        }

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
