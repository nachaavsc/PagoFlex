package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.RecuperarClaveUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecuperarClaveViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarClaveUIState())
    val uiState: StateFlow<RecuperarClaveUIState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                correoError = Validador.revalidar(it.correoError) { Validador.correo(valor) },
                errorGeneral = null
            )
        }
    }

    fun onTelefonoChange(valor: String) {
        _uiState.update {
            it.copy(
                telefono = valor,
                telefonoError = Validador.revalidar(it.telefonoError) { Validador.telefono(valor) },
                errorGeneral = null
            )
        }
    }

    fun onClaveChange(valor: String) {
        _uiState.update {
            it.copy(
                clave = valor,
                claveError = Validador.revalidar(it.claveError) { Validador.clave(valor) },
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

    fun restablecerClave() {
        val state = _uiState.value
        val validado = state.copy(
            correoError = Validador.correo(state.correo),
            telefonoError = Validador.telefono(state.telefono),
            claveError = Validador.clave(state.clave),
            confirmarClaveError = Validador.confirmarClave(state.clave, state.confirmarClave),
            errorGeneral = null
        )
        _uiState.value = validado
        val hayErrores = listOf(
            validado.correoError,
            validado.telefonoError,
            validado.claveError,
            validado.confirmarClaveError
        ).any { it != null }
        if (hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val restablecida = runCatching { repository.restablecerClave(state.correo, state.telefono, state.clave) }.getOrDefault(false)
            _uiState.update {
                if (restablecida) {
                    it.copy(cargando = false, mensajeExito = "Clave actualizada. Ya puedes iniciar sesión.")
                } else {
                    it.copy(cargando = false, errorGeneral = "No encontramos una cuenta con ese correo y teléfono")
                }
            }
        }
    }

    fun onMensajeMostrado() {
        _uiState.value = RecuperarClaveUIState()
    }
}
