package com.alessandra.entrenaria.ui.screens.signup

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    auth: FirebaseAuth,
    navigateToHome: () -> Unit = {}
) {
    // Toma los contenidos de los TextFields y los guarda en variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // sirve para mostrar Toast
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
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

            Spacer(Modifier.height(32.dp))

            // Contraseña
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // Ocultar caracteres contraseña
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            Spacer(Modifier.height(32.dp))

            // Botón iniciar sesión
            Button(
                onClick = {

                    // Validaciones
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_LONG).show()
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(context, "Introduce un email válido", Toast.LENGTH_LONG).show()

                    } else if (password.length < 6) {
                        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                // Si la navegación es correcta, crea un usuario en Firestore
                                if (task.isSuccessful) {
                                    val db = Firebase.firestore
                                    val user = FirebaseAuth.getInstance().currentUser
                                    user?.let {
                                        // Tomamos el id y email de Firebase y los guardamos en Firestore
                                        val userData = hashMapOf(
                                            "uid" to it.uid,
                                            "email" to it.email
                                        )
                                        db.collection("users")
                                            .document(it.uid)
                                            .set(userData)
                                            .addOnSuccessListener {
                                                //Log.d("Firestore", "Usuario creado en Firestore correctamente")
                                                navigateToHome()
                                            }
                                            .addOnFailureListener { e ->
                                                //Log.w("Firestore", "Error al crear usuario en Firestore", e)
                                                Toast.makeText(context, "Error al crear usuario en Firestore", Toast.LENGTH_LONG).show()
                                                auth.currentUser?.delete()?.addOnSuccessListener {
                                                    auth.signOut()
                                                }
                                            }
                                    }

                                } else {
                                    // Si no, muestra una alerta
                                    val exception = task.exception
                                    if (exception is FirebaseAuthUserCollisionException) {
                                        Toast.makeText(context, "Este email ya está registrado", Toast.LENGTH_LONG).show()
                                    } else {

                                        Toast.makeText(
                                            context,
                                            "Error al registrarse: ${exception?.localizedMessage ?: "Desconocido"}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Registrarse")
            }
        }
    }
}

