package com.alessandra.entrenaria.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Timestamp.formatAsDate(): String {
    val date = this.toDate()
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale("es"))
    return formatter.format(date)
}

fun Date.formatAsDate(): String {
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale("es"))
    return formatter.format(this)
}