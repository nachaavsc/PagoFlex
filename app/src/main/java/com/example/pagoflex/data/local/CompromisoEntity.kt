package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object EstadoPago {
    const val PENDIENTE = "PENDIENTE"
    const val PAGADO = "PAGADO"
    const val VENCIDO = "VENCIDO"
}

@Entity(
    tableName = "compromisos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["folio"], unique = true),
        Index("usuarioId"),
        Index("estado"),
        Index("organizacion")
    ]
)
data class CompromisoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val folio: String,
    val organizacion: String,
    val concepto: String,
    val cuotaActual: Int,
    val totalCuotas: Int,
    val monto: Long,
    val fechaVencimiento: Long,
    val estado: String = EstadoPago.PENDIENTE
)
