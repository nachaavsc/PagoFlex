package com.example.pagoflex.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.TransaccionEntity
import com.example.pagoflex.data.repository.PagoFlexRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PagoFlexRepository

    val transacciones: StateFlow<List<TransaccionEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).transaccionDao()
        repository = PagoFlexRepository(dao)

        transacciones = repository.todasLasTransacciones.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}