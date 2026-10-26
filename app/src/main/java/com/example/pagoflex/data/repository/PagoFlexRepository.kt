package com.example.pagoflex.data.repository

import android.content.Context
import com.example.pagoflex.data.local.AppDatabase
import com.example.pagoflex.data.local.TransaccionDao
import com.example.pagoflex.data.model.Transaccion
import com.example.pagoflex.data.model.toDomain
import com.example.pagoflex.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PagoFlexRepository(context: Context) {

    private val dao: TransaccionDao = AppDatabase.getDatabase(context).transaccionDao()

    fun obtenerTransacciones(): Flow<List<Transaccion>> {
        return dao.obtenerTodas().map { lista ->
            lista.map { it.toDomain() }
        }
    }

    suspend fun insertarTransaccion(transaccion: Transaccion) {
        dao.insertar(transaccion.toEntity())
    }
}