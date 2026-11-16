package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PerfilUIState(
    val nombreUsuario: String = "Usuario PagoFlex",
    val correoUsuario: String = "usuario@pagoflex.com",
    val cuentaActiva: Boolean = true
)

class PerfilViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUIState())
    val uiState: StateFlow<PerfilUIState> = _uiState.asStateFlow()

    fun cerrarSesion() {
    }
}
