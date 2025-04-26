package com.alessandra.entrenaria.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun InitialScreen(){
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ){
        Text("Prueba de color", color = MaterialTheme.colorScheme.primary)
    }
}

@Preview
@Composable
fun InitialScreenPreview(){
    EntrenarIATheme {
        InitialScreen()
    }
}