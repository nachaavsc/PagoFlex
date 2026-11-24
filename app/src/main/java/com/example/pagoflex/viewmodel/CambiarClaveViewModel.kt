package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.CambiarClaveUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CambiarClaveViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CambiarClaveUIState())
    val uiState: StateFlow<CambiarClaveUIState> = _uiState.asStateFlow()

    fun onClaveActualChange(valor: String) {
        _uiState.update {
            it.copy(
                claveActual = valor,
                claveActualError = Validador.revalidar(it.claveActualError) { validarClaveActual(valor) }
            )
        }
    }

    fun onClaveChange(valor: String) {
        _uiState.update {
            it.copy(
                clave = valor,
                claveError = Validador.revalidar(it.claveError) { validarNuevaClave(it.claveActual, valor) },
                confirmarClaveError = Validador.revalidar(it.confirmarClaveError) {
                    Validador.confirmarClave(valor, it.confirmarClave)
                }
            )
        }
    }

    fun onConfirmarClaveChange(valor: String) {
        _uiState.update {
            it.copy(
                confirmarClave = valor,
                confirmarClaveError = Validador.revalidar(it.confirmarClaveError) {
                    Validador.confirmarClave(it.clave, valor)
                }
            )
        }
    }

    private fun validarClaveActual(clave: String): String? =
        Validador.obligatorio(clave, "Ingresa tu clave actual")

    private fun validarNuevaClave(claveActual: String, nuevaClave: String): String? =
        Validador.clave(nuevaClave)
            ?: if (nuevaClave == claveActual) "La nueva clave debe ser distinta a la actual" else null

    fun cambiarClave() {
        val state = _uiState.value
        val validado = state.copy(
            claveActualError = validarClaveActual(state.claveActual),
            claveError = validarNuevaClave(state.claveActual, state.clave),
            confirmarClaveError = Validador.confirmarClave(state.clave, state.confirmarClave)
        )
        _uiState.value = validado
        if (listOf(validado.claveActualError, validado.claveError, validado.confirmarClaveError).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val cambiada = runCatching { repository.cambiarClave(state.claveActual, state.clave) }.getOrDefault(false)
            _uiState.update {
                if (cambiada) {
                    CambiarClaveUIState(mensajeExito = "Clave actualizada correctamente")
                } else {
                    it.copy(cargando = false, claveActualError = "La clave actual no es correcta")
                }
            }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensajeExito = null) }
    }
}
