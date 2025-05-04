package com.alessandra.entrenaria.ui.screens.initial

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alessandra.entrenaria.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun InitialScreen(
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    naviageToHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->
                        if (authResult.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            if (firebaseUser != null) {
                                val uid = firebaseUser.uid
                                val email = firebaseUser.email

                                val userDocRef = db.collection("users").document(uid)

                                userDocRef.get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            Log.d("InitialScreen", "Usuario ya existe en Firestore")
                                            naviageToHome()
                                        } else {
                                            val newUser = hashMapOf(
                                                "uid" to uid,
                                                "email" to email,
                                                "name" to "",
                                                "age" to 0,
                                                "gender" to ""
                                            )

                                            userDocRef.set(newUser, SetOptions.merge())
                                                .addOnSuccessListener {
                                                    Log.d("InitialScreen", "Nuevo usuario creado en Firestore")
                                                    naviageToHome()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("InitialScreen", "Error al crear nuevo usuario", e)
                                                    Toast.makeText(context, "Error creando usuario", Toast.LENGTH_SHORT).show()
                                                    naviageToHome()
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("InitialScreen", "Error verificando existencia de usuario", e)
                                        Toast.makeText(context, "Error al obtener datos de usuario", Toast.LENGTH_SHORT).show()
                                        naviageToHome()
                                    }
                            }
                        } else {
                            Log.e("InitialScreen", "Error en login con Google", authResult.exception)
                            Toast.makeText(context, "Error en inicio de sesión con Google", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("InitialScreen", "Google Sign In falló", e)
            Toast.makeText(context, "Error en autenticación con Google", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary // ✅ Color de fondo aplicado aquí
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.entrenaria_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )

            Text(
                "Entrena de forma inteligente. Progresa sin límites.",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navigateToSignUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Regístrate con Email", color = MaterialTheme.colorScheme.onSecondary)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Continúa con Google",
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(16.dp)
                                .align(Alignment.CenterStart)
                        )
                        Text(
                            "Continúa con Google",
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { navigateToLogin() }) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
