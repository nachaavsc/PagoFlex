package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object EstadoIncidencia {
    const val EN_REVISION = "En revisión"
    const val RESUELTA = "Resuelta"
}

@Entity(
    tableName = "incidencias",
    indices = [Index(value = ["folio"], unique = true), Index("estado")]
)
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val folio: String,
    val motivo: String,
    val detalle: String,
    val fechaReporte: Long,
    val estado: String = EstadoIncidencia.EN_REVISION,
    val comprobante: String
)
