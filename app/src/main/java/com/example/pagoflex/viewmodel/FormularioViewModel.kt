package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.FormularioUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PagoFlexRepository(AppDatabase.getDatabase(application))

    private val _uiState = MutableStateFlow(FormularioUIState())
    val uiState: StateFlow<FormularioUIState> = _uiState.asStateFlow()

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, nombreError = null) }
        validarFormulario()
    }

    fun onCorreoChange(nuevoCorreo: String) {
        _uiState.update { it.copy(correo = nuevoCorreo, correoError = null) }
        validarFormulario()
    }

    fun onMontoChange(nuevoMonto: String) {
        _uiState.update { it.copy(monto = nuevoMonto, montoError = null) }
        validarFormulario()
    }

    private fun validarFormulario() {
        val state = _uiState.value
        val esNombreValido = state.nombre.isNotBlank()
        val esCorreoValido = state.correo.contains("@") && state.correo.contains(".")
        val esMontoValido = state.monto.toDoubleOrNull()?.let { it > 0 } ?: false

        _uiState.update {
            it.copy(esValido = esNombreValido && esCorreoValido && esMontoValido)
        }
    }

    fun guardarTransaccion() {
        val state = _uiState.value

        viewModelScope.launch {
            val nuevaTransaccion = TransaccionEntity(
                concepto = "Pago a ${state.nombre}",
                monto = state.monto.toDoubleOrNull() ?: 0.0,
                fecha = "2026-10-27",
                estado = "Completado"
            )
            repository.agregarTransaccion(nuevaTransaccion)
            _uiState.update { it.copy(guardadoExitoso = true) }
        }
    }

    fun resetGuardado() {
        _uiState.update { FormularioUIState() }
    }
}