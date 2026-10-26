package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String,
    val monto: Double,
    val fecha: String = "2026-10-25",
    val estado: String = "Completado"
)
