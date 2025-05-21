package com.alessandra.entrenaria.user.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.core.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.core.ui.commonComponents.BottomBarItem
import com.alessandra.entrenaria.core.ui.commonComponents.handleBottomBarNavigation
import com.alessandra.entrenaria.user.ViewModel.UserViewModel
import com.alessandra.entrenaria.user.ViewModel.UserViewModelFactory
import com.alessandra.entrenaria.user.data.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    onNavigateToTrainings: () -> Unit,
    onNavigateToUser: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val repository = remember { UserRepository() }
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(repository)
    )

    val user by viewModel.user.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Perfil de usuario", color = MaterialTheme.colorScheme.onSurface)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreenBottomBarItem = BottomBarItem.UserItem,
                onNavigate = { destination ->
                    handleBottomBarNavigation(
                        destination = destination,
                        onTrainings = onNavigateToTrainings,
                        onUser = onNavigateToUser,
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
                ProfileField(label = "Email", value = user?.email ?: "No disponible")
                ProfileField(label = "Nombre", value = user?.name ?: "No definido")
                ProfileField(label = "Género", value = user?.gender ?: "No definido")
                ProfileField(label = "Edad", value = user?.age?.toString() ?: "No definido")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Editar perfil", color = MaterialTheme.colorScheme.onPrimary)
            }

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

        if (showEditDialog && user != null) {
            EditUserDialog(
                currentName = user?.name ?: "",
                currentAge = user?.age,
                currentGender = user?.gender,
                onDismiss = { showEditDialog = false },
                onUserUpdated = {
                    showEditDialog = false
                    viewModel.loadUserData()
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
