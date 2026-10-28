package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.EstadoPago
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalPorPagar: Long = 0L,
    val proximoVencimiento: Long? = null,
    val cantidadVencidos: Int = 0,
    val cuentasPorPagar: List<CompromisoEntity> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PagoFlexRepository(AppDatabase.getDatabase(application))

    val uiState: StateFlow<HomeUiState> = repository.compromisosNoPagados
        .map { cuentas ->
            HomeUiState(
                totalPorPagar = cuentas.sumOf { it.monto },
                proximoVencimiento = cuentas
                    .filter { it.estado == EstadoPago.PENDIENTE }
                    .minOfOrNull { it.fechaVencimiento },
                cantidadVencidos = cuentas.count { it.estado == EstadoPago.VENCIDO },
                cuentasPorPagar = cuentas
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    private val _recordatoriosActivos = MutableStateFlow(false)
    val recordatoriosActivos: StateFlow<Boolean> = _recordatoriosActivos.asStateFlow()

    fun onRecordatoriosChange(activos: Boolean) {
        _recordatoriosActivos.value = activos
    }
}
