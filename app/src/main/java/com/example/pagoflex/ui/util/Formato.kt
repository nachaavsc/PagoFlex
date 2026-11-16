package com.example.pagoflex.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val localeChile = Locale("es", "CL")

fun formatearMonto(monto: Long): String = "$" + String.format(localeChile, "%,d", monto)

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd-MM-yyyy", localeChile).format(Date(millis))

fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm", localeChile).format(Date(millis))

fun fechaUtcALocal(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}
