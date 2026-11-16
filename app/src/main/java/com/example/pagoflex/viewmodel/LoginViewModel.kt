package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.LoginUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor,
                correoError = Validador.revalidar(it.correoError) { Validador.correo(valor) },
                errorGeneral = null
            )
        }
    }

    fun onClaveChange(valor: String) {
        _uiState.update {
            it.copy(
                clave = valor,
                claveError = Validador.revalidar(it.claveError) { validarClave(valor) },
                errorGeneral = null
            )
        }
    }

    private fun validarClave(clave: String): String? =
        Validador.obligatorio(clave, "La clave es obligatoria")

    fun iniciarSesion() {
        val state = _uiState.value
        val correoError = Validador.correo(state.correo)
        val claveError = validarClave(state.clave)
        _uiState.update { it.copy(correoError = correoError, claveError = claveError, errorGeneral = null) }
        if (correoError != null || claveError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val usuario = repository.iniciarSesion(state.correo, state.clave)
            _uiState.update {
                if (usuario == null) {
                    it.copy(cargando = false, errorGeneral = "Correo o clave incorrectos")
                } else {
                    it.copy(cargando = false, clave = "", rolAutenticado = usuario.rol)
                }
            }
        }
    }

    fun onNavegacionCompletada() {
        _uiState.value = LoginUIState()
    }
}
