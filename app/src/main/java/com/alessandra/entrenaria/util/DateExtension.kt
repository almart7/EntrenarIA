package com.alessandra.entrenaria.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Timestamp.formatAsDate(): String {
    val date = this.toDate()
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale("es"))
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

fun Date.formatAsDate(): String {
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale("es"))
    return formatter.format(this).replaceFirstChar { it.uppercase() }
}