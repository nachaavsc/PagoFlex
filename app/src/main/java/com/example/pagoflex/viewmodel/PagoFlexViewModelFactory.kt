package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones

class PagoFlexViewModelFactory(
    private val repository: PagoFlexRepository,
    private val notificaciones: GestorNotificaciones,
    private val almacenImagenes: AlmacenImagenes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository)
        modelClass.isAssignableFrom(FormularioViewModel::class.java) -> FormularioViewModel(repository)
        modelClass.isAssignableFrom(CompromisosViewModel::class.java) -> CompromisosViewModel(repository)
        modelClass.isAssignableFrom(HistorialViewModel::class.java) -> HistorialViewModel(repository)
        modelClass.isAssignableFrom(IncidenciasViewModel::class.java) -> IncidenciasViewModel(repository)
        modelClass.isAssignableFrom(PerfilViewModel::class.java) -> PerfilViewModel(repository, almacenImagenes)
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository)
        modelClass.isAssignableFrom(RegistroViewModel::class.java) -> RegistroViewModel(repository)
        modelClass.isAssignableFrom(RecuperarClaveViewModel::class.java) -> RecuperarClaveViewModel(repository)
        modelClass.isAssignableFrom(CambiarClaveViewModel::class.java) -> CambiarClaveViewModel(repository)
        modelClass.isAssignableFrom(ConfirmarPagoViewModel::class.java) -> ConfirmarPagoViewModel(repository, notificaciones)
        modelClass.isAssignableFrom(NuevoCompromisoViewModel::class.java) -> NuevoCompromisoViewModel(repository, notificaciones)
        else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
    } as T
}
