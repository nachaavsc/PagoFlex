package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CompromisosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PagoFlexRepository(AppDatabase.getDatabase(application))

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    val compromisos: StateFlow<List<CompromisoEntity>> = _filtroEstado
        .flatMapLatest { estado -> repository.compromisosPorEstado(estado) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conteoPorEstado: StateFlow<Map<String, Int>> = repository.compromisos
        .map { lista -> lista.groupingBy { it.estado }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun onFiltroChange(estado: String?) {
        _filtroEstado.value = estado
    }

    fun registrarPago(compromiso: CompromisoEntity) {
        viewModelScope.launch {
            repository.registrarPago(compromiso, canal = "App Móvil PagoFlex", ahora = System.currentTimeMillis())
        }
    }
}
