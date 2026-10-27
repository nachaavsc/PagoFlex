package com.example.pagoflex.data.repository

import com.example.pagoflex.data.local.TransaccionDao
import com.example.pagoflex.data.local.TransaccionEntity
import kotlinx.coroutines.flow.Flow

class PagoFlexRepository(private val dao: TransaccionDao) {
    val todasLasTransacciones: Flow<List<TransaccionEntity>> = dao.obtenerTodas()

    suspend fun agregarTransaccion(transaccion: TransaccionEntity) {
        dao.insertarTransaccion(transaccion)
    }
}