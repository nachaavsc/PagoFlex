package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.IncidenciaUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IncidenciaConCliente(
    val incidencia: IncidenciaEntity,
    val nombreCliente: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class IncidenciasViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    val esGestor: StateFlow<Boolean> = repository.usuarioActual
        .map { it?.rol == RolUsuario.GESTOR }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val incidencias: StateFlow<List<IncidenciaConCliente>> = repository.usuarioActual
        .flatMapLatest { usuario ->
            when {
                usuario == null -> flowOf(emptyList())
                usuario.rol == RolUsuario.GESTOR -> combine(repository.incidencias, repository.clientes) { incidencias, clientes ->
                    val nombres = clientes.associate { it.id to it.nombre }
                    incidencias.map { IncidenciaConCliente(it, nombres[it.usuarioId]) }
                }
                else -> repository.incidenciasDeUsuario(usuario.id).map { lista ->
                    lista.map { IncidenciaConCliente(it) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(IncidenciaUIState())
    val uiState: StateFlow<IncidenciaUIState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoUI>(Channel.BUFFERED)
    val eventos: Flow<EventoUI> = _eventos.receiveAsFlow()

    fun onMotivoChange(valor: String) {
        _uiState.update { it.copy(motivo = valor, motivoError = validarMotivo(valor)) }
    }

    fun onDetalleChange(valor: String) {
        _uiState.update { it.copy(detalle = valor, detalleError = validarDetalle(valor)) }
    }

    private fun validarMotivo(motivo: String): String? =
        Validador.obligatorio(motivo, "Selecciona el motivo de la incidencia")

    private fun validarDetalle(detalle: String): String? = when {
        detalle.isBlank() -> "Describe lo que ocurrió"
        detalle.trim().length < 10 -> "La descripción debe tener al menos 10 caracteres"
        else -> null
    }

    fun reportarIncidencia() {
        val state = _uiState.value
        val motivoError = validarMotivo(state.motivo)
        val detalleError = validarDetalle(state.detalle)
        _uiState.update { it.copy(motivoError = motivoError, detalleError = detalleError) }
        if (motivoError != null || detalleError != null) {
            _eventos.trySend(EventoUI.Error("Revisa los campos marcados"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val incidencia = repository.reportarIncidencia(state.motivo, state.detalle.trim(), System.currentTimeMillis())
            if (incidencia == null) {
                _uiState.update { it.copy(cargando = false) }
                _eventos.send(EventoUI.Error("Tu sesión expiró. Inicia sesión nuevamente."))
            } else {
                _uiState.value = IncidenciaUIState()
                _eventos.send(EventoUI.Exito("Incidencia ${incidencia.folio} enviada. Te avisaremos cuando sea revisada."))
            }
        }
    }

    fun resolverIncidencia(incidencia: IncidenciaEntity) {
        viewModelScope.launch {
            repository.resolverIncidencia(incidencia.id)
            _eventos.send(EventoUI.Exito("Incidencia ${incidencia.folio} marcada como resuelta"))
        }
    }
}
