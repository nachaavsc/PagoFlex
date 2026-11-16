package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagos",
    foreignKeys = [
        ForeignKey(
            entity = CompromisoEntity::class,
            parentColumns = ["id"],
            childColumns = ["compromisoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["comprobante"], unique = true),
        Index("compromisoId"),
        Index("organizacion"),
        Index("fechaPago")
    ]
)
data class PagoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val compromisoId: Int,
    val montoPagado: Long,
    val fechaPago: Long,
    val rutaComprobanteFoto: String? = null,
    val folio: String,
    val organizacion: String,
    val concepto: String,
    val cuotaActual: Int,
    val totalCuotas: Int,
    val canal: String,
    val comprobante: String
)

fun CompromisoEntity.generarPago(
    fechaPago: Long,
    canal: String,
    rutaComprobanteFoto: String? = null
) = PagoEntity(
    compromisoId = id,
    montoPagado = monto,
    fechaPago = fechaPago,
    rutaComprobanteFoto = rutaComprobanteFoto,
    folio = folio,
    organizacion = organizacion,
    concepto = concepto,
    cuotaActual = cuotaActual,
    totalCuotas = totalCuotas,
    canal = canal,
    comprobante = "PF-PAGO-$fechaPago"
)
