package com.example.pagoflex.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object RolUsuario {
    const val CLIENTE = "CLIENTE"
    const val GESTOR = "GESTOR"
}

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true), Index("rol")]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: String = RolUsuario.CLIENTE,
    val telefono: String = "",
    val direccion: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
)
