package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.FormularioUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

fun validarNombre(nombre: String): String? =
    if (nombre.isBlank()) "El nombre es obligatorio" else null

fun validarCorreo(correo: String): String? = when {
    correo.isBlank() -> "El correo es obligatorio"
    !correo.contains("@") -> "El correo debe contener @"
    else -> null
}

fun validarMonto(monto: String): String? =
    if (monto.isBlank()) "El monto es obligatorio" else null

fun validarContrasena(contrasena: String): String? =
    if (contrasena.isBlank()) "La contraseña es obligatoria" else null

class FormularioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUIState())
    val uiState: StateFlow<FormularioUIState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(nombre = valor, nombreError = validarNombre(valor)).conValidez() }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, correoError = validarCorreo(valor)).conValidez() }
    }

    fun onMontoChange(valor: String) {
        _uiState.update { it.copy(monto = valor, montoError = validarMonto(valor)).conValidez() }
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update {
            it.copy(contrasena = valor, contrasenaError = validarContrasena(valor)).conValidez()
        }
    }

    fun guardarTransaccion() {
        _uiState.update {
            val validado = it.copy(
                nombreError = validarNombre(it.nombre),
                correoError = validarCorreo(it.correo),
                montoError = validarMonto(it.monto),
                contrasenaError = validarContrasena(it.contrasena)
            ).conValidez()
            validado.copy(guardadoExitoso = validado.esValido)
        }
    }

    fun resetGuardado() {
        _uiState.update { it.copy(guardadoExitoso = false) }
    }

    private fun FormularioUIState.conValidez(): FormularioUIState = copy(
        esValido = validarNombre(nombre) == null &&
            validarCorreo(correo) == null &&
            validarMonto(monto) == null &&
            validarContrasena(contrasena) == null
    )
}
