package com.example.pagoflex.viewmodel

sealed interface EventoUI {
    val mensaje: String

    data class Exito(override val mensaje: String) : EventoUI

    data class Error(override val mensaje: String) : EventoUI
}
