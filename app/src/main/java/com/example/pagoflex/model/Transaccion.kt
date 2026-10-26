package com.example.pagoflex.model

import com.example.pagoflex.data.local.TransaccionEntity

data class Transaccion(
    val id: Int = 0,
    val concepto: String,
    val monto: Double,
    val estado: String
)

fun TransaccionEntity.toDomain() = Transaccion(
    id = id,
    concepto = concepto,
    monto = monto,
    estado = estado
)

fun Transaccion.toEntity() = TransaccionEntity(
    id = id,
    concepto = concepto,
    monto = monto,
    estado = estado
)