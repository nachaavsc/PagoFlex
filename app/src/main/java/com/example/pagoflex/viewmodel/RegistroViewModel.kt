package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.AuthRepository
import com.example.pagoflex.model.RegistroUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: AuthRepository) : ViewModel() {

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

            // Ahora usamos el repositorio de Retrofit para enviar los datos al Gateway
            val resultado = repository.registro(
                nombre = state.nombre,
                correo = state.correo,
                clave = state.clave,
                telefono = state.telefono
            )

            resultado.onSuccess { respuesta ->
                _uiState.update { it.copy(cargando = false, rolAutenticado = respuesta.rol) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        cargando = false,
                        correoError = error.message ?: "Error al crear la cuenta"
                    )
                }
            }
        }
    }

    fun onNavegacionCompletada() {
        _uiState.value = RegistroUIState()
    }
}
class RegistroViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}