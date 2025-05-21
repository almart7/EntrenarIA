import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alessandra.entrenaria.chat.data.model.ChatMessage

@Composable
fun ChatBubble(message: ChatMessage) {
    // Determina si el mensaje fue enviado por el usuario
    val isUser = message.sender == "user"

    Row(
        // Alineación horizontal: a la derecha si es del usuario, izquierda si es de la IA
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Espaciado entre burbujas
    ) {
        Surface(
            // Color diferente para mensajes del usuario y del asistente (basado en el tema)
            color = if (isUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp), // Bordes redondeados para la burbuja
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Contenido del mensaje
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}