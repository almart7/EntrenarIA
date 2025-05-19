package com.alessandra.entrenaria.util

import android.app.DatePickerDialog
import com.google.firebase.Timestamp
import java.util.*

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
