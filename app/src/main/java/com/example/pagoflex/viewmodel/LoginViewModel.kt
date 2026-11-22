package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.repository.AuthRepository
import com.example.pagoflex.model.LoginResponse
import com.example.pagoflex.model.LoginUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.Result

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    fun onCorreoChange(correo: String) {
        _uiState.value = _uiState.value.copy(
            correo = correo,
            correoError = Validador.revalidar(_uiState.value.correoError) { Validador.correo(correo) }
        )
    }

    fun onClaveChange(clave: String) {
        _uiState.value = _uiState.value.copy(
            clave = clave,
            claveError = Validador.revalidar(_uiState.value.claveError) { Validador.clave(clave) }
        )
    }

    fun hacerLogin() {
        val state = _uiState.value
        val errorCorreo = Validador.correo(state.correo)
        val errorClave = Validador.clave(state.clave)

        if (errorCorreo != null || errorClave != null) {
            _uiState.value = state.copy(correoError = errorCorreo, claveError = errorClave)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, errorGeneral = null)

            val resultado: Result<LoginResponse> = repository.login(state.correo, state.clave)

            resultado.onSuccess { respuesta ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    rolAutenticado = respuesta.rol
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorGeneral = error.message ?: "Error al iniciar sesión"
                )
            }
        }
    }
}

class LoginViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}