package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

enum class PeriodoHistorial(val etiqueta: String, val dias: Long) {
    ULTIMO_MES("Último mes", 30),
    TRES_MESES("3 meses", 90),
    DOCE_MESES("12 meses", 365);

    fun desde(ahora: Long): Long = ahora - TimeUnit.DAYS.toMillis(dias)
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.sincronizar() }
    }

    private val _periodo = MutableStateFlow(PeriodoHistorial.TRES_MESES)
    val periodo: StateFlow<PeriodoHistorial> = _periodo.asStateFlow()

    private val _empresa = MutableStateFlow<String?>(null)
    val empresa: StateFlow<String?> = _empresa.asStateFlow()

    private val historial: StateFlow<List<PagoEntity>> = repository.usuarioActual
        .flatMapLatest { usuario ->
            usuario?.let { repository.historialDeUsuario(it.id) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val empresas: StateFlow<List<String>> = historial
        .map { pagos -> pagos.map { it.organizacion }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagos: StateFlow<List<PagoEntity>> = combine(historial, _periodo, _empresa) { lista, periodo, empresa ->
        val desde = periodo.desde(System.currentTimeMillis())
        lista.filter { it.fechaPago >= desde && (empresa == null || it.organizacion == empresa) }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPeriodoChange(periodo: PeriodoHistorial) {
        _periodo.value = periodo
    }

    fun onEmpresaChange(empresa: String?) {
        _empresa.value = empresa
    }
}
