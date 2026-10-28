package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    indices = [Index(value = ["comprobante"], unique = true), Index("empresa"), Index("fechaPago")]
)
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val cuotaActual: Int,
    val totalCuotas: Int,
    val monto: Long,
    val fechaPago: Long,
    val canal: String,
    val estado: String = EstadoPago.PAGADO,
    val comprobante: String
)
