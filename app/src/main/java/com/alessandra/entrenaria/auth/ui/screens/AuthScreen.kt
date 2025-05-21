package com.alessandra.entrenaria.auth.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.R

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Configuración Google Sign-In (igual que antes)
    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = androidx.compose.runtime.remember {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error en autenticación con Google", Toast.LENGTH_SHORT).show()
        }
    }

    val loginState by viewModel.loginState.collectAsState()

    when (loginState) {
        is AuthViewModel.LoginState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthViewModel.LoginState.Success -> {
            LaunchedEffect(Unit) {
                navigateToHome()
            }
        }
        is AuthViewModel.LoginState.Error -> {
            val error = (loginState as AuthViewModel.LoginState.Error).message
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            // Evitar bucle de errores
            LaunchedEffect(error) {
                viewModel._loginState.value = AuthViewModel.LoginState.Idle
            }
        }
        else -> {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.primary
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

                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.entrenaria_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(200.dp)
                    )

                    // Eslogan
                    Text(
                        "Entrena de forma inteligente",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp, bottom = 48.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Login/Registro con Google
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

                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

