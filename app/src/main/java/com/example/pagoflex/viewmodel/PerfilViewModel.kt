package com.example.pagoflex.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.PerfilUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repository: PagoFlexRepository,
    private val almacenImagenes: AlmacenImagenes
) : ViewModel() {

    val usuario: StateFlow<UsuarioEntity?> = repository.usuarioActual

    private val _uiState = MutableStateFlow(PerfilUIState())
    val uiState: StateFlow<PerfilUIState> = _uiState.asStateFlow()

    private val _fotoPerfil = MutableStateFlow<Bitmap?>(null)
    val fotoPerfil: StateFlow<Bitmap?> = _fotoPerfil.asStateFlow()

    private val _eventos = Channel<EventoUI>(Channel.BUFFERED)
    val eventos: Flow<EventoUI> = _eventos.receiveAsFlow()

    init {
        repository.usuarioActual.value?.let { actual ->
            _uiState.value = PerfilUIState(
                nombre = actual.nombre,
                correo = actual.correo,
                telefono = actual.telefono,
                direccion = actual.direccion
            )
            viewModelScope.launch {
                almacenImagenes.versionFotos.collect {
                    _fotoPerfil.value = almacenImagenes.cargarFotoPerfil(actual.id)
                }
            }
        }
    }

    fun onFotoTomada(bitmap: Bitmap) {
        guardarFoto { id -> almacenImagenes.guardarFotoPerfil(id, bitmap) }
    }

    fun onImagenSeleccionada(uri: Uri) {
        guardarFoto { id -> almacenImagenes.guardarFotoPerfil(id, uri) }
    }

    fun onErrorFoto(mensaje: String) {
        _eventos.trySend(EventoUI.Error(mensaje))
    }

    private fun guardarFoto(guardar: suspend (Int) -> Bitmap?) {
        val id = usuario.value?.id ?: return
        viewModelScope.launch {
            val foto = guardar(id)
            if (foto == null) {
                _eventos.send(EventoUI.Error("No se pudo guardar la imagen seleccionada"))
            } else {
                _eventos.send(EventoUI.Exito("Foto de perfil actualizada"))
            }
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
