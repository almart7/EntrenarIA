package com.alessandra.entrenaria.data.repository

import android.util.Log
import com.alessandra.entrenaria.BuildConfig
import com.alessandra.entrenaria.data.model.ChatMessage
import com.alessandra.entrenaria.data.model.ExerciseWithContext
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.database.*

class ChatRepository(private val database: FirebaseDatabase) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendMessage(userId: String, message: ChatMessage) {
        val ref = database.getReference("chats/$userId/messages").push()
        val messageWithId = message.copy(messageId = ref.key ?: "")
        ref.setValue(messageWithId)
    }

    fun observeMessages(userId: String, onData: (List<ChatMessage>) -> Unit) {
        val ref = database.getReference("chats/$userId/messages")
        ref.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull {
                    it.getValue(ChatMessage::class.java)
                }
                onData(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Error loading messages", error.toException())
            }
        })
    }


    suspend fun generateGeminiResponse(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun buildPromptFromExercises(exercises: List<ExerciseWithContext>, question: String): String {
        val context = exercises.joinToString("\n") {
            val name = it.exerciseData["name"] ?: "Sin nombre"
            val sets = it.exerciseData["sets"] ?: "N/A"
            "- $name (sets: $sets)"
        }

        return """
            Eres un entrenador personal con varios años de experiencia.
            Estos son mis entrenamientos del último mes:

            $context

            Basado en esa información, responde lo siguiente:
            $question
        """.trimIndent()
    }
}
