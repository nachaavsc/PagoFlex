package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transacciones")
data class TransaccionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String,
    val monto: Double,
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "Completado"
)
