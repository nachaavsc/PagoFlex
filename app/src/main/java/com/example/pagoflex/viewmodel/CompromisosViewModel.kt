package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.local.CompromisoEntity
import com.example.pagoflex.data.local.RolUsuario
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
import kotlinx.coroutines.launch

data class CompromisoConCliente(
    val compromiso: CompromisoEntity,
    val nombreCliente: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CompromisosViewModel(private val repository: PagoFlexRepository) : ViewModel() {

    init {
        viewModelScope.launch { repository.sincronizar() }
    }

    val esGestor: StateFlow<Boolean> = repository.usuarioActual
        .map { it?.rol == RolUsuario.GESTOR }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _filtroEstado = MutableStateFlow<String?>(null)
    val filtroEstado: StateFlow<String?> = _filtroEstado.asStateFlow()

    private val todos: StateFlow<List<CompromisoConCliente>> = repository.usuarioActual
        .flatMapLatest { usuario ->
            when {
                usuario == null -> flowOf(emptyList())
                usuario.rol == RolUsuario.GESTOR -> combine(repository.compromisos, repository.clientes) { compromisos, clientes ->
                    val nombres = clientes.associate { it.id to it.nombre }
                    compromisos.map { CompromisoConCliente(it, nombres[it.usuarioId]) }
                }
                else -> repository.compromisosDeUsuario(usuario.id).map { lista ->
                    lista.map { CompromisoConCliente(it) }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val compromisos: StateFlow<List<CompromisoConCliente>> = combine(todos, _filtroEstado) { lista, estado ->
        if (estado == null) lista else lista.filter { it.compromiso.estado == estado }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conteoPorEstado: StateFlow<Map<String, Int>> = todos
        .map { lista -> lista.groupingBy { it.compromiso.estado }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun onFiltroChange(estado: String?) {
        _filtroEstado.value = estado
    }
}
