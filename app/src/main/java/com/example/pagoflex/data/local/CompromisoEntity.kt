package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object EstadoPago {
    const val PAGADO = "Pagado"
    const val PENDIENTE = "Pendiente"
    const val VENCIDO = "Vencido"
}

@Entity(
    tableName = "compromisos",
    indices = [Index(value = ["folio"], unique = true), Index("estado"), Index("empresa")]
)
data class CompromisoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val cuotaActual: Int,
    val totalCuotas: Int,
    val monto: Long,
    val fechaVencimiento: Long,
    val estado: String = EstadoPago.PENDIENTE
)
