package com.alessandra.entrenaria.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.ui.commonComponents.BottomBarItem
import com.alessandra.entrenaria.ui.commonComponents.handleBottomBarNavigation // ✅ Importa la función
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    fun loadUserData() {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid)
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

    LaunchedEffect(Unit) {
        loadUserData()
    }

    Scaffold(
        // Barra superior con el título de la pantalla
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil de usuario",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Barra de navegación inferior común
        bottomBar = {
            BottomNavigationBar(
                currentDestination = BottomBarItem.ProfileItem,
                onNavigate = { destination ->
                    handleBottomBarNavigation(
                        destination = destination,
                        onTrainings = onNavigateToTrainings,
                        onProfile = onNavigateToProfile,
                        onChat = onNavigateToChat
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileField(label = "Email", value = email)
                ProfileField(label = "Nombre", value = name ?: "No definido")
                ProfileField(label = "Género", value = gender ?: "No definido")
                ProfileField(label = "Edad", value = age?.toString() ?: "No definido")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para abrir el diálogo de edición de perfil
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Editar perfil", color = MaterialTheme.colorScheme.onPrimary)
            }

            // Botón para cerrar sesión
            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
            }
        }

        // Diálogo de edición de perfil
        if (showEditDialog) {
            EditProfileDialog(
                currentName = name ?: "",
                currentAge = age,
                currentGender = gender,
                onDismiss = { showEditDialog = false },
                onProfileUpdated = {
                    showEditDialog = false
                    loadUserData()
                }
            )
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}
