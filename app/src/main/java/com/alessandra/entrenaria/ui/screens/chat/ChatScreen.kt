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
import androidx.navigation.NavController
import com.alessandra.entrenaria.data.repository.ChatRepository
import com.alessandra.entrenaria.navigation.Chat
import com.alessandra.entrenaria.ui.components.BottomNavigationBar
import com.alessandra.entrenaria.ui.components.ChatBubble
import com.alessandra.entrenaria.ui.viewmodel.ChatViewModel
import com.alessandra.entrenaria.ui.viewmodel.ChatViewModelFactory
import com.entrenaria.models.TrainingRepository
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ChatScreen(userId: String, navController: NavController) {
    val chatRepository = remember { ChatRepository(FirebaseDatabase.getInstance()) }
    val trainingRepository = remember { TrainingRepository() }

    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository, trainingRepository, userId)
    )

    val uiState by viewModel.uiState
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, currentDestination = Chat)
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f), reverseLayout = true) {
                items(uiState.messages.reversed()) { ChatBubble(it) }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe tu mensaje...") }
                )
                IconButton(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
