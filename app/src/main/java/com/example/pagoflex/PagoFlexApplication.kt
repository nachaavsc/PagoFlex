package com.example.pagoflex

import android.app.Application
import com.example.pagoflex.data.archivos.AlmacenImagenes
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.notificaciones.GestorNotificaciones
import com.example.pagoflex.viewmodel.PagoFlexViewModelFactory

class PagoFlexApplication : Application() {

    val repository: PagoFlexRepository by lazy {
        PagoFlexRepository(AppDatabase.getDatabase(this))
    }

    val notificaciones: GestorNotificaciones by lazy { GestorNotificaciones(this) }

    val almacenImagenes: AlmacenImagenes by lazy { AlmacenImagenes(this) }

    val viewModelFactory: PagoFlexViewModelFactory by lazy {
        PagoFlexViewModelFactory(repository, notificaciones, almacenImagenes)
    }

    override fun onCreate() {
        super.onCreate()
        notificaciones.crearCanal()
    }
}
