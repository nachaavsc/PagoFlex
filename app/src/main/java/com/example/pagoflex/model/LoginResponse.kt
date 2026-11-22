package com.example.pagoflex.model

data class LoginResponse(
    val id: Long,
    val usuarioId: Long,
    val correo: String,
    val rol: String
)