package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.PagoEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit

enum class PeriodoHistorial(val etiqueta: String, val dias: Long) {
    ULTIMO_MES("Último mes", 30),
    TRES_MESES("3 meses", 90),
    DOCE_MESES("12 meses", 365);

    fun desde(ahora: Long): Long = ahora - TimeUnit.DAYS.toMillis(dias)
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PagoFlexRepository(AppDatabase.getDatabase(application))

    private val _periodo = MutableStateFlow(PeriodoHistorial.ULTIMO_MES)
    val periodo: StateFlow<PeriodoHistorial> = _periodo.asStateFlow()

    private val _empresa = MutableStateFlow<String?>(null)
    val empresa: StateFlow<String?> = _empresa.asStateFlow()

    val empresas: StateFlow<List<String>> = repository.empresasConPagos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagos: StateFlow<List<PagoEntity>> = combine(_periodo, _empresa) { periodo, empresa ->
        periodo.desde(System.currentTimeMillis()) to empresa
    }
        .flatMapLatest { (desde, empresa) -> repository.pagosFiltrados(desde, empresa) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPeriodoChange(periodo: PeriodoHistorial) {
        _periodo.value = periodo
    }

    fun onEmpresaChange(empresa: String?) {
        _empresa.value = empresa
    }
}
