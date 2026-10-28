package com.example.pagoflex.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeChile = Locale("es", "CL")

fun formatearMonto(monto: Long): String = "$" + String.format(localeChile, "%,d", monto)

fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd-MM-yyyy", localeChile).format(Date(millis))

fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm", localeChile).format(Date(millis))
