package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.RegistroUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUIState())
    val uiState: StateFlow<RegistroUIState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) {
        _uiState.update {
            it.copy(nombre = valor, nombreError = Validador.revalidar(it.nombreError) { Validador.nombre(valor) })
        }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update {
            it.copy(correo = valor, correoError = Validador.revalidar(it.correoError) { Validador.correo(valor) })
        }
    }

    fun onTelefonoChange(valor: String) {
        _uiState.update {
            it.copy(telefono = valor, telefonoError = Validador.revalidar(it.telefonoError) { Validador.telefono(valor) })
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

    fun crearCuenta() {
        val state = _uiState.value
        val validado = state.copy(
            nombreError = Validador.nombre(state.nombre),
            correoError = Validador.correo(state.correo),
            telefonoError = Validador.telefono(state.telefono),
            claveError = Validador.clave(state.clave),
            confirmarClaveError = Validador.confirmarClave(state.clave, state.confirmarClave)
        )
        _uiState.value = validado
        val hayErrores = listOf(
            validado.nombreError,
            validado.correoError,
            validado.telefonoError,
            validado.claveError,
            validado.confirmarClaveError
        ).any { it != null }
        if (hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                repository.registrarUsuario(state.nombre, state.correo, state.telefono, state.clave)
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, correoError = e.message ?: "No se pudo crear la cuenta") }
                return@launch
            }
            val usuario = repository.iniciarSesion(state.correo, state.clave)
            _uiState.update { it.copy(cargando = false, rolAutenticado = usuario?.rol) }
        }
    }

    fun onNavegacionCompletada() {
        _uiState.value = RegistroUIState()
    }
}
