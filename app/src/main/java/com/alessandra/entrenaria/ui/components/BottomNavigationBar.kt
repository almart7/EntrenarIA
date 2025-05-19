package com.alessandra.entrenaria.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.alessandra.entrenaria.navigation.TrainingPeriods
import com.alessandra.entrenaria.navigation.Profile
import com.alessandra.entrenaria.navigation.Chat

// Define cada ítem del menú inferior
sealed class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val destination: Any
) {
    object HomeItem : BottomBarItem("Inicio", Icons.Default.Home, TrainingPeriods)
    object ProfileItem : BottomBarItem("Perfil", Icons.Default.Person, Profile)
    object ChatItem : BottomBarItem("Entrenador IA", Icons.Default.ChatBubble, Chat)
}

@Composable
fun BottomNavigationBar(
    currentDestination: Any?,
    onNavigate: (Any) -> Unit
) {
    val items = listOf(
        BottomBarItem.HomeItem,
        BottomBarItem.ProfileItem,
        BottomBarItem.ChatItem
    )

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination == item.destination

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigate(item.destination)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

// Función reutilizable para gestionar la navegación en el menú inferior
fun handleBottomBarNavigation(
    destination: Any,
    onTrainings: () -> Unit,
    onProfile: () -> Unit,
    onChat: () -> Unit
) {
    when (destination) {
        BottomBarItem.HomeItem.destination -> onTrainings()
        BottomBarItem.ProfileItem.destination -> onProfile()
        BottomBarItem.ChatItem.destination -> onChat()
    }
}
