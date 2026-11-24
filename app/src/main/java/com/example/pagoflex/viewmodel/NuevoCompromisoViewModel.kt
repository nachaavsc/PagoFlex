package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones
import com.example.pagoflex.model.NuevoCompromisoUIState
import com.example.pagoflex.model.Validador
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NuevoCompromisoViewModel(
    private val repository: PagoFlexRepository,
    private val notificaciones: GestorNotificaciones
) : ViewModel() {

    val clientes: StateFlow<List<UsuarioEntity>> = repository.clientes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(NuevoCompromisoUIState())
    val uiState: StateFlow<NuevoCompromisoUIState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoUI>(Channel.BUFFERED)
    val eventos: Flow<EventoUI> = _eventos.receiveAsFlow()

    fun onClienteChange(cliente: UsuarioEntity) {
        _uiState.update { it.copy(clienteId = cliente.id, clienteNombre = cliente.nombre, clienteError = null) }
    }

    fun onOrganizacionChange(valor: String) {
        _uiState.update { it.copy(organizacion = valor, organizacionError = validarOrganizacion(valor)) }
    }

    fun onConceptoChange(valor: String) {
        _uiState.update { it.copy(concepto = valor, conceptoError = validarConcepto(valor)) }
    }

    fun onMontoChange(valor: String) {
        _uiState.update { it.copy(monto = valor, montoError = Validador.monto(valor)) }
    }

    fun onFechaVencimientoChange(fecha: Long) {
        _uiState.update { it.copy(fechaVencimiento = fecha, fechaError = null) }
    }

    private fun validarOrganizacion(valor: String): String? =
        Validador.obligatorio(valor, "La organización es obligatoria")

    private fun validarConcepto(valor: String): String? =
        Validador.obligatorio(valor, "El concepto es obligatorio")

    fun guardarCompromiso() {
        val state = _uiState.value
        val validado = state.copy(
            clienteError = if (state.clienteId == null) "Selecciona el cliente" else null,
            organizacionError = validarOrganizacion(state.organizacion),
            conceptoError = validarConcepto(state.concepto),
            montoError = Validador.monto(state.monto),
            fechaError = if (state.fechaVencimiento == null) "Selecciona la fecha de vencimiento" else null
        )
        _uiState.value = validado
        val hayErrores = listOf(
            validado.clienteError,
            validado.organizacionError,
            validado.conceptoError,
            validado.montoError,
            validado.fechaError
        ).any { it != null }
        if (hayErrores) {
            _eventos.trySend(EventoUI.Error("Revisa los campos marcados"))
            return
        }

        val clienteId = state.clienteId ?: return
        val fechaVencimiento = state.fechaVencimiento ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            val compromiso = try {
                repository.registrarCompromiso(
                    clienteId = clienteId,
                    organizacion = state.organizacion,
                    concepto = state.concepto,
                    monto = state.monto.trim().toLong(),
                    fechaVencimiento = fechaVencimiento
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false) }
                _eventos.send(EventoUI.Error(e.message ?: "No se pudo registrar el compromiso"))
                return@launch
            }
            notificaciones.notificarCompromisoAsignado(compromiso, state.clienteNombre)
            _uiState.value = NuevoCompromisoUIState()
            _eventos.send(EventoUI.Exito("Compromiso ${compromiso.folio} asignado a ${state.clienteNombre}"))
        }
    }
}
