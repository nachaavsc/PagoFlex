package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.IncidenciaEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncidenciasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PagoFlexRepository(AppDatabase.getDatabase(application))

    val incidencias: StateFlow<List<IncidenciaEntity>> = repository.incidencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun reportarIncidencia(motivo: String, detalle: String) {
        viewModelScope.launch {
            repository.reportarIncidencia(motivo, detalle.trim(), System.currentTimeMillis())
        }
    }
}
