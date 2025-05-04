package com.alessandra.entrenaria.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.alessandra.entrenaria.navigation.Home
import com.alessandra.entrenaria.navigation.Profile
import com.alessandra.entrenaria.navigation.Chat

sealed class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val destination: Any
) {
    object HomeItem : BottomBarItem("Inicio", Icons.Default.Home, Home)
    object ProfileItem : BottomBarItem("Perfil", Icons.Default.Person, Profile)
    object ChatItem : BottomBarItem("Chat", Icons.Default.Call, Chat)
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentDestination: Any?
) {
    NavigationBar {
        val items = listOf(
            BottomBarItem.HomeItem,
            BottomBarItem.ProfileItem,
            BottomBarItem.ChatItem
        )

        items.forEach { item ->
            val selected = currentDestination == item.destination
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.destination)
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
