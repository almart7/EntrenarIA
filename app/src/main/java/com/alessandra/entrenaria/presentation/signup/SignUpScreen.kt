package com.alessandra.entrenaria.presentation.signup

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alessandra.entrenaria.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SignUpScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {},
    navigateToProfile: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(24.dp)
                    .clickable { navigateBack() }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = "Email",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = "Password",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    showToast(context, "Por favor, completa todos los campos")
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showToast(context, "Introduce un email válido")
                } else if (password.length < 6) {
                    showToast(context, "La contraseña debe tener al menos 6 caracteres")
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Crear el usuario en Firestore
                                val db = Firebase.firestore

                                val user = FirebaseAuth.getInstance().currentUser
                                user?.let {
                                    val userData = hashMapOf(
                                        "uid" to it.uid,
                                        "email" to it.email
                                    )

                                    db.collection("users")
                                        .document(it.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Usuario creado en Firestore correctamente")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("Firestore", "Error al crear usuario en Firestore", e)
                                        }
                                }

                                // ir a la pagina de perfil
                                navigateToProfile()
                            } else {
                                val exception = task.exception
                                if (exception is FirebaseAuthUserCollisionException) {
                                    showToast(context, "Este email ya está registrado")
                                } else {
                                    showToast(
                                        context,
                                        "Error al registrarse: ${exception?.localizedMessage ?: "Desconocido"}"
                                    )
                                }
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign Up")
        }
    }
}

// 🔥 Función utilitaria para mostrar Toast
private fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
