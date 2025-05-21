package com.alessandra.entrenaria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.alessandra.entrenaria.core.navigation.NavigationWrapper
import com.alessandra.entrenaria.core.ui.theme.EntrenarIATheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Permite que el contenido se dibuje debajo de la barra de estado
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            EntrenarIATheme {
                val colorScheme = MaterialTheme.colorScheme
                val useDarkIcons = !isSystemInDarkTheme()

                // Cambiar el color de la barra de estado y el color de iconos
                val window = this.window
                val decorView = window.decorView
                val wic = WindowInsetsControllerCompat(window, decorView)

                // Ponemos el color de fondo para la barra de estado
                window.statusBarColor = colorScheme.background.toArgb()
                // Configuramos los iconos: oscuros en tema claro, claros en oscuro
                wic.isAppearanceLightStatusBars = useDarkIcons

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background,
                ) {
                    NavigationWrapper(auth)
                }
            }
        }
    }
}
