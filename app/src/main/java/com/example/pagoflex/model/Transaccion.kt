package com.example.pagoflex.model

data class Transaccion(
    val id: Int = 0,
    val concepto: String = "",
    val monto: Double = 0.0,
    val fecha: String = "",
    val estado: String = "Pendiente"
)