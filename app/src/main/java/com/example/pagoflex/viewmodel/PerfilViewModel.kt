package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PerfilUIState(
    val nombreUsuario: String = "Usuario PagoFlex",
    val correoUsuario: String = "usuario@pagoflex.com",
    val cuentaActiva: Boolean = true
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PerfilUIState())
    val uiState: StateFlow<PerfilUIState> = _uiState.asStateFlow()

    fun cerrarSesion() {
    }
}