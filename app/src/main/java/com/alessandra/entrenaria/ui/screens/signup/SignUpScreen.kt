package com.alessandra.entrenaria.ui.screens.signup

import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {},
    navigateToHome: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )

            Spacer(Modifier.height(32.dp))

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
                                    navigateToHome()
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
                Text(text = "Registrarse")
            }
        }
    }
}

private fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
