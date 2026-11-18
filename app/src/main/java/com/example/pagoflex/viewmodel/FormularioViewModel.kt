package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.FormularioUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FormularioViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUIState())
    val uiState: StateFlow<FormularioUIState> = _uiState.asStateFlow()

    fun onNombreChange(nuevoNombre: String) {
        _uiState.update {
            it.copy(nombre = nuevoNombre, nombreError = Validador.revalidar(it.nombreError) { Validador.nombre(nuevoNombre) })
        }
    }

    fun onCorreoChange(nuevoCorreo: String) {
        _uiState.update {
            it.copy(correo = nuevoCorreo, correoError = Validador.revalidar(it.correoError) { Validador.correo(nuevoCorreo) })
        }
    }

    fun onMontoChange(nuevoMonto: String) {
        _uiState.update {
            it.copy(monto = nuevoMonto, montoError = Validador.revalidar(it.montoError) { Validador.monto(nuevoMonto) })
        }
    }

    fun onClaveChange(nuevaClave: String) {
        _uiState.update {
            it.copy(clave = nuevaClave, claveError = Validador.revalidar(it.claveError) { validarClave(nuevaClave) })
        }
    }

    private fun validarClave(clave: String): String? =
        Validador.obligatorio(clave, "Ingresa tu clave para confirmar el pago")
            ?: if (repository.verificarClave(clave)) null else "La clave no coincide con tu cuenta"

    fun guardarTransaccion() {
        val state = _uiState.value
        if (state.cargando) return
        val validado = state.copy(
            nombreError = Validador.nombre(state.nombre),
            correoError = Validador.correo(state.correo),
            montoError = Validador.monto(state.monto),
            claveError = validarClave(state.clave)
        )
        _uiState.value = validado
        val hayErrores = listOf(validado.nombreError, validado.correoError, validado.montoError, validado.claveError)
            .any { it != null }
        if (hayErrores) return

        _uiState.update { it.copy(cargando = true) }
        viewModelScope.launch {
            val nuevaTransaccion = TransaccionEntity(
                concepto = "Pago a ${state.nombre.trim()}",
                monto = state.monto.trim().toDouble(),
                fecha = System.currentTimeMillis(),
                estado = "Completado"
            )
            repository.agregarTransaccion(nuevaTransaccion)
            _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
        }
    }

    fun resetGuardado() {
        _uiState.update { FormularioUIState() }
    }
}
