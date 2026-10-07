package com.example.pagoflex.data

data class Transaccion(
    val id: String,
    val titulo: String,
    val monto: Double,
    val fecha: String,
    val esIngreso: Boolean
)
