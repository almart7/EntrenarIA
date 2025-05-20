package com.alessandra.entrenaria.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alessandra.entrenaria.data.repository.ChatRepository
import com.alessandra.entrenaria.ui.commonComponents.BottomBarItem
import com.alessandra.entrenaria.ui.commonComponents.BottomNavigationBar
import com.alessandra.entrenaria.ui.commonComponents.handleBottomBarNavigation
import com.alessandra.entrenaria.ui.viewmodel.ChatViewModel
import com.alessandra.entrenaria.ui.viewmodel.ChatViewModelFactory
import com.alessandra.entrenaria.model.TrainingRepository
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ChatScreen(
    userId: String,
    onNavigateToTrainings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    // Instancias de los repositorios que usará el ViewModel
    val chatRepository = remember { ChatRepository(FirebaseDatabase.getInstance()) }
    val trainingRepository = remember { TrainingRepository() }

    // ViewModel con una factory personalizada que inyecta ambos repositorios y el userId
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository, trainingRepository, userId)
    )

    // Estado del chat (mensajes, estado de carga, etc.)
    val uiState by viewModel.uiState

    // Estado local del campo de texto donde el usuario escribe
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            // Menú inferior común en toda la app
            BottomNavigationBar(
                currentDestination = BottomBarItem.ChatItem,
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
                .padding(16.dp)
        ) {
            // Lista de mensajes (invertida: los últimos abajo)
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true // Muestra los mensajes desde el final
            ) {
                // Recorre y muestra cada mensaje usando una burbuja
                items(uiState.messages.reversed()) { message ->
                    ChatBubble(message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de entrada + botón enviar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Campo de texto donde se escribe el mensaje
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe tu mensaje...") }
                )

                // Botón de enviar
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim()) // Envía el mensaje al ViewModel
                        inputText = "" // Limpia el campo tras enviar
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
