package com.alessandra.entrenaria.util

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog

/**
 * Formatea el Timestamp de forma mas legible
 */
fun Timestamp.formatAsDate(): String {
    val date = this.toDate()
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale("es"))
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

/**
 * Formatea el Date de forma mas legible
 */
fun Date.formatAsDate(): String {
    val formatter = SimpleDateFormat("EEEE d MMMM yyyy", Locale("es"))
    return formatter.format(this).replaceFirstChar { it.uppercase() }
}

/**
 * Devuelve true si la fecha proporcionada representa el día de hoy.
 */
fun isToday(date: Timestamp): Boolean {
    return date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
}

/**
 * Devuelve true si la fecha de hoy está entre start y end (inclusive) del periodo.
 */
fun isTodayInRange(start: Timestamp, end: Timestamp?): Boolean {
    val today = LocalDate.now()
    val startDate = start.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val endDate = end?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: startDate
    return today in startDate..endDate
}


fun showDatePicker(
    context: android.content.Context,
    preselected: Timestamp? = null,
    minDate: Date? = null,
    maxDate: Date? = null,
    onDateSelected: (Timestamp) -> Unit
) {
    val calendar = Calendar.getInstance()
    if (preselected != null) calendar.time = preselected.toDate()

    val picker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(Timestamp(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (minDate != null) picker.datePicker.minDate = minDate.time
    if (maxDate != null) picker.datePicker.maxDate = maxDate.time

    picker.show()
}
