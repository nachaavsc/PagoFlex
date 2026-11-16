package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.PerfilUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    val usuario: StateFlow<UsuarioEntity?> = repository.usuarioActual

    private val _uiState = MutableStateFlow(PerfilUIState())
    val uiState: StateFlow<PerfilUIState> = _uiState.asStateFlow()

    init {
        repository.usuarioActual.value?.let { actual ->
            _uiState.value = PerfilUIState(
                nombre = actual.nombre,
                correo = actual.correo,
                telefono = actual.telefono,
                direccion = actual.direccion
            )
        }
    }

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

    fun onDireccionChange(valor: String) {
        _uiState.update { it.copy(direccion = valor) }
    }

    fun guardarPerfil() {
        val state = _uiState.value
        val validado = state.copy(
            nombreError = Validador.nombre(state.nombre),
            correoError = Validador.correo(state.correo),
            telefonoError = Validador.telefono(state.telefono)
        )
        _uiState.value = validado
        if (listOf(validado.nombreError, validado.correoError, validado.telefonoError).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            if (repository.existeCorreo(state.correo, excluirId = usuario.value?.id)) {
                _uiState.update { it.copy(cargando = false, correoError = "Ya existe una cuenta con este correo") }
                return@launch
            }
            repository.actualizarPerfil(state.nombre, state.correo, state.telefono, state.direccion)
            _uiState.update { it.copy(cargando = false, mensajeExito = "Perfil actualizado correctamente") }
        }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
    }
}
