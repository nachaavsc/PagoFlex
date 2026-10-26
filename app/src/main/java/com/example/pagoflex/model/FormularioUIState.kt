package com.example.pagoflex.model

data class FormularioUIState(
    val nombre: String = "",
    val nombreError: String? = null,
    val correo: String = "",
    val correoError: String? = null,
    val monto: String = "",
    val montoError: String? = null,
    val esValido: Boolean = false,
    val guardadoExitoso: Boolean = false
)