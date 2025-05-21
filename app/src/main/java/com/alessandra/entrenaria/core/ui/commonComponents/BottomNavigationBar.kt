package com.alessandra.entrenaria.core.ui.commonComponents

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.alessandra.entrenaria.core.navigation.TrainingPeriods
import com.alessandra.entrenaria.core.navigation.User
import com.alessandra.entrenaria.core.navigation.Chat

// Define cada ítem del menú inferior
sealed class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val destination: Any  // Pagina actual
) {
    object TrainingsItem : BottomBarItem("Tus entrenamientos", Icons.Default.FitnessCenter, TrainingPeriods)
    object UserItem : BottomBarItem("Perfil", Icons.Default.Person, User)
    object ChatItem : BottomBarItem("Entrenador IA", Icons.Default.ChatBubble, Chat)
}

@Composable
fun BottomNavigationBar(
    currentScreenBottomBarItem: BottomBarItem?,
    onNavigate: (Any) -> Unit
) {
    val items = listOf(
        BottomBarItem.TrainingsItem,
        BottomBarItem.UserItem,
        BottomBarItem.ChatItem
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        items.forEach { loopItem ->
            // Compara por tipo para cubrir casos donde currentDestination es instancia diferente pero del mismo destino
            val selected = currentScreenBottomBarItem?.destination == loopItem.destination

            NavigationBarItem(
                selected = selected,
                onClick = {
                    // No navega si ya está en esa pagina
                    if (currentScreenBottomBarItem?.destination != loopItem.destination) {
                        onNavigate(loopItem.destination)
                    }
                },
                icon = {
                    Icon(
                        imageVector = loopItem.icon,
                        contentDescription = loopItem.label
                    )
                },
                label = { Text(loopItem.label) },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

// Función reutilizable para gestionar la navegación en el menú inferior
fun handleBottomBarNavigation(
    destination: Any,
    onTrainings: () -> Unit,
    onUser: () -> Unit,
    onChat: () -> Unit
) {
    when (destination) {
        TrainingPeriods -> onTrainings()
        User -> onUser()
        Chat -> onChat()
    }
}
