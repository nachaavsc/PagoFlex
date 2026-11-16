package com.example.pagoflex.viewmodel

import android.database.SQLException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmarPagoUIState(
    val compromiso: CompromisoEntity? = null,
    val cargando: Boolean = false,
    val pagoRegistrado: PagoEntity? = null
)

class ConfirmarPagoViewModel(
    private val repository: PagoFlexRepository,
    private val notificaciones: GestorNotificaciones
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmarPagoUIState())
    val uiState: StateFlow<ConfirmarPagoUIState> = _uiState.asStateFlow()

    private val _eventos = Channel<EventoUI>(Channel.BUFFERED)
    val eventos: Flow<EventoUI> = _eventos.receiveAsFlow()

    fun cargar(compromisoId: Int) {
        if (_uiState.value.compromiso?.id == compromisoId) return
        viewModelScope.launch {
            val compromiso = repository.obtenerCompromiso(compromisoId)
                ?.takeIf { it.usuarioId == repository.usuarioActual.value?.id }
            if (compromiso == null) {
                _eventos.send(EventoUI.Error("No encontramos el compromiso seleccionado"))
            }
            _uiState.update { it.copy(compromiso = compromiso) }
        }
    }

    fun confirmarPago() {
        val state = _uiState.value
        val compromiso = state.compromiso ?: return
        if (state.cargando || state.pagoRegistrado != null) return
        if (compromiso.estado == EstadoPago.PAGADO) {
            _eventos.trySend(EventoUI.Error("Este compromiso ya fue pagado"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val pago = repository.registrarPago(compromiso, CANAL_APP, System.currentTimeMillis())
                _uiState.update {
                    it.copy(
                        compromiso = compromiso.copy(estado = EstadoPago.PAGADO),
                        cargando = false,
                        pagoRegistrado = pago
                    )
                }
                notificaciones.notificarPago(pago)
                _eventos.send(EventoUI.Exito("Pago registrado con éxito"))
            } catch (e: SQLException) {
                _uiState.update { it.copy(cargando = false) }
                _eventos.send(EventoUI.Error("No se pudo registrar el pago. Intenta nuevamente."))
            }
        }
    }

    private companion object {
        const val CANAL_APP = "App Móvil PagoFlex"
    }
}
