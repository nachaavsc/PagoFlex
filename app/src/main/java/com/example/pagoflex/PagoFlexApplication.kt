package com.example.pagoflex

import android.app.Application
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.repository.PagoFlexRepository
import com.example.pagoflex.viewmodel.PagoFlexViewModelFactory

class PagoFlexApplication : Application() {

    val repository: PagoFlexRepository by lazy {
        PagoFlexRepository(AppDatabase.getDatabase(this))
    }

    val viewModelFactory: PagoFlexViewModelFactory by lazy {
        PagoFlexViewModelFactory(repository)
    }
}
