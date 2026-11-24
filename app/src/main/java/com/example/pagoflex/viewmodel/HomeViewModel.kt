package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoIncidencia
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.local.RolUsuario
import com.example.pagoflex.data.local.UsuarioEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.model.Cumplimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val nombreUsuario: String = "",
    val rol: String = RolUsuario.CLIENTE,
    val totalPorPagar: Long = 0L,
    val proximoVencimiento: Long? = null,
    val cantidadVencidos: Int = 0,
    val cuentasPorPagar: List<CompromisoEntity> = emptyList(),
    val porcentajeCumplimiento: Int = 0,
    val pagosRealizados: Int = 0,
    val totalCompromisos: Int = 0,
    val rachaAlDia: Int = 0,
    val totalClientes: Int = 0,
    val montoRecaudado: Long = 0L,
    val incidenciasAbiertas: Int = 0
) {
    val esGestor: Boolean get() = rol == RolUsuario.GESTOR
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.sincronizar() }
    }

    val usuarioActual: StateFlow<UsuarioEntity?> = repository.usuarioActual

    val uiState: StateFlow<HomeUiState> = repository.usuarioActual
        .flatMapLatest { usuario ->
            when {
                usuario == null -> flowOf(HomeUiState())
                usuario.rol == RolUsuario.GESTOR -> combine(
                    repository.compromisos,
                    repository.clientes,
                    repository.incidencias
                ) { compromisos, clientes, incidencias ->
                    resumenGestor(usuario, compromisos, clientes.size, incidencias)
                }
                else -> combine(
                    repository.compromisosDeUsuario(usuario.id),
                    repository.historialDeUsuario(usuario.id)
                ) { compromisos, pagos ->
                    resumenCliente(usuario, compromisos, pagos)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    private fun resumenCliente(
        usuario: UsuarioEntity,
        compromisos: List<CompromisoEntity>,
        pagos: List<PagoEntity>
    ): HomeUiState {
        val porPagar = compromisos.filter { it.estado != EstadoPago.PAGADO }
        return HomeUiState(
            nombreUsuario = usuario.nombre,
            rol = usuario.rol,
            totalPorPagar = porPagar.sumOf { it.monto },
            proximoVencimiento = porPagar
                .filter { it.estado == EstadoPago.PENDIENTE }
                .minOfOrNull { it.fechaVencimiento },
            cantidadVencidos = porPagar.count { it.estado == EstadoPago.VENCIDO },
            cuentasPorPagar = porPagar,
            porcentajeCumplimiento = Cumplimiento.porcentaje(compromisos),
            pagosRealizados = compromisos.count { it.estado == EstadoPago.PAGADO },
            totalCompromisos = compromisos.size,
            rachaAlDia = Cumplimiento.racha(pagos, compromisos)
        )
    }

    private fun resumenGestor(
        usuario: UsuarioEntity,
        compromisos: List<CompromisoEntity>,
        totalClientes: Int,
        incidencias: List<IncidenciaEntity>
    ): HomeUiState {
        val porCobrar = compromisos.filter { it.estado != EstadoPago.PAGADO }
        return HomeUiState(
            nombreUsuario = usuario.nombre,
            rol = usuario.rol,
            totalPorPagar = porCobrar.sumOf { it.monto },
            cantidadVencidos = porCobrar.count { it.estado == EstadoPago.VENCIDO },
            porcentajeCumplimiento = Cumplimiento.porcentaje(compromisos),
            pagosRealizados = compromisos.count { it.estado == EstadoPago.PAGADO },
            totalCompromisos = compromisos.size,
            totalClientes = totalClientes,
            montoRecaudado = compromisos.filter { it.estado == EstadoPago.PAGADO }.sumOf { it.monto },
            incidenciasAbiertas = incidencias.count { it.estado == EstadoIncidencia.EN_REVISION }
        )
    }
}
